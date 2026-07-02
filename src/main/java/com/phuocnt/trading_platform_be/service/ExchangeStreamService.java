package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.dto.ws.KlineMessage;
import com.phuocnt.trading_platform_be.dto.ws.PriceMessage;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeStreamService implements ApplicationRunner {

    private final PricePublisher pricePublisher;
    private final CoinCacheService coinCacheService;
    private final ObjectMapper objectMapper;

    @Value("${binance.stream.enabled:true}")
    private boolean enabled;

    private static final Map<String, String> SYMBOL_TO_COIN_ID = Map.ofEntries(
            Map.entry("BTCUSDT", "bitcoin"),
            Map.entry("ETHUSDT", "ethereum"),
            Map.entry("BNBUSDT", "binancecoin"),
            Map.entry("SOLUSDT", "solana"),
            Map.entry("XRPUSDT", "ripple"),
            Map.entry("ADAUSDT", "cardano"),
            Map.entry("DOGEUSDT", "dogecoin"),
            Map.entry("AVAXUSDT", "avalanche-2"),
            Map.entry("DOTUSDT", "polkadot"),
            Map.entry("MATICUSDT", "matic-network")
    );

    private static final Map<String, String> SYMBOL_TO_NAME = Map.ofEntries(
            Map.entry("BTCUSDT", "Bitcoin"),
            Map.entry("ETHUSDT", "Ethereum"),
            Map.entry("BNBUSDT", "BNB"),
            Map.entry("SOLUSDT", "Solana"),
            Map.entry("XRPUSDT", "XRP"),
            Map.entry("ADAUSDT", "Cardano"),
            Map.entry("DOGEUSDT", "Dogecoin"),
            Map.entry("AVAXUSDT", "Avalanche"),
            Map.entry("DOTUSDT", "Polkadot"),
            Map.entry("MATICUSDT", "Polygon")
    );

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private volatile boolean running = true;

    private volatile WebSocket webSocket;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("[Binance] Stream disabled — skipping");
            return;
        }

        connect();
    }

    private void connect() {
        if (!running) {
            return;
        }

        String url = buildStreamUrl();

        log.info("[Binance] Connecting to stream: {}", url);

        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(url), new BinanceListener())
                .thenAccept(ws -> {

                    if (!running) {
                        ws.abort();
                        return;
                    }

                    this.webSocket = ws;

                    ws.request(1);

                    log.info("[Binance] Connected successfully");
                })
                .exceptionally(e -> {
                    if (running) {
                        log.error("[Binance] Connection failed: {}", e.getMessage());
                        scheduleReconnect();
                    }
                    return null;
                });
    }

    private String buildStreamUrl() {
        List<String> streams = SYMBOL_TO_COIN_ID.keySet()
                .stream()
                .map(symbol -> {
                    String s = symbol.toLowerCase();
                    return s + "@ticker/" + s + "@kline_1m";
                })
                .collect(Collectors.toList());

        return "wss://stream.binance.com:9443/stream?streams="
                + String.join("/", streams);
    }

    private void scheduleReconnect() {

        if (!running || scheduler.isShutdown()) {
            return;
        }

        scheduler.schedule(() -> {

            if (!running) {
                return;
            }

            log.info("[Binance] Reconnecting...");
            connect();

        }, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {

        log.info("[Binance] Shutting down stream service");

        running = false;

        try {
            if (webSocket != null) {
                webSocket.abort();
                log.info("[Binance] WebSocket closed");
            }
        } catch (Exception e) {
            log.warn("[Binance] Failed to close WebSocket", e);
        }

        try {

            scheduler.shutdownNow();

            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("[Binance] Scheduler did not terminate cleanly");
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        log.info("[Binance] Stream service stopped");
    }

    private class BinanceListener implements WebSocket.Listener {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(
                WebSocket ws,
                CharSequence data,
                boolean last
        ) {

            buffer.append(data);

            if (last) {
                handleMessage(buffer.toString());
                buffer.setLength(0);
            }

            ws.request(1);

            return null;
        }

        @Override
        public CompletionStage<?> onClose(
                WebSocket ws,
                int statusCode,
                String reason
        ) {

            log.warn(
                    "[Binance] Stream closed: {} - {}",
                    statusCode,
                    reason
            );

            if (running) {
                scheduleReconnect();
            }

            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {

            log.error(
                    "[Binance] Stream error: {}",
                    error.getMessage()
            );

            if (running) {
                scheduleReconnect();
            }
        }
    }

    private void handleMessage(String json) {

        try {

            JsonNode root = objectMapper.readTree(json);

            String stream = root.path("stream").asText();

            JsonNode data = root.path("data");

            if (stream.endsWith("@ticker")) {

                handleTicker(data);

            } else if (stream.contains("@kline_")) {

                handleKline(data);
            }

        } catch (Exception e) {

            log.warn(
                    "[Binance] Failed to parse message: {}",
                    e.getMessage()
            );
        }
    }

    private void handleTicker(JsonNode data) {

        if (!running) {
            return;
        }

        String binanceSymbol = data.path("s").asText();

        String coinId = SYMBOL_TO_COIN_ID.get(binanceSymbol);

        if (coinId == null) {
            return;
        }

        String price = data.path("c").asText();
        String open24h = data.path("o").asText();
        String high24h = data.path("h").asText();
        String low24h = data.path("l").asText();
        String volume = data.path("v").asText();
        String change = data.path("P").asText();

        coinCacheService.savePrice(coinId, price);

        PriceMessage msg = PriceMessage.builder()
                .type("PRICE_UPDATE")
                .coinId(coinId)
                .symbol(binanceSymbol.replace("USDT", ""))
                .name(SYMBOL_TO_NAME.getOrDefault(
                        binanceSymbol,
                        binanceSymbol
                ))
                .price(price)
                .open24h(open24h)
                .high24h(high24h)
                .low24h(low24h)
                .volume24h(volume)
                .change24h(change)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        pricePublisher.broadcast(msg);
    }

    private void handleKline(JsonNode data) {

        if (!running) {
            return;
        }

        String binanceSymbol = data.path("s").asText();

        String coinId = SYMBOL_TO_COIN_ID.get(binanceSymbol);

        if (coinId == null) {
            return;
        }

        JsonNode k = data.path("k");

        KlineMessage kline = KlineMessage.builder()
                .type("KLINE_UPDATE")
                .coinId(coinId)
                .symbol(binanceSymbol.replace("USDT", ""))
                .interval(k.path("i").asText())
                .openTime(k.path("t").asLong())
                .open(k.path("o").asText())
                .high(k.path("h").asText())
                .low(k.path("l").asText())
                .close(k.path("c").asText())
                .volume(k.path("v").asText())
                .closed(k.path("x").asBoolean())
                .timestamp(Instant.now().toEpochMilli())
                .build();

        pricePublisher.broadcastKline(kline);
    }
}