package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.dto.response.KlineResponse;
import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.repository.CoinRepository;
import com.phuocnt.trading_platform_be.service.CoinCacheService;
import com.phuocnt.trading_platform_be.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoinServiceImpl implements CoinService {

    private final CoinRepository coinRepository;
    private final ObjectMapper objectMapper;
    private final CoinCacheService coinCacheService;

    @Value("${coingecko.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ConcurrentHashMap<Integer, Object> pageLocks = new ConcurrentHashMap<>();

    private String callApi(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("CoinGecko API error: " + e.getMessage());
        }
    }

    @Override
    public List<Coin> getCoinsList(int page) {
        var cached = coinCacheService.getCoinList(page);
        if (cached.isPresent()) return cached.get();

        Object lock = pageLocks.computeIfAbsent(page, k -> new Object());
        synchronized (lock) {
            cached = coinCacheService.getCoinList(page);
            if (cached.isPresent()) return cached.get();

            // Chỉ 1 thread đến được đây
            log.info("[CoinGecko] Fetching page {} (cache miss)", page);
            String url = baseUrl + "/coins/markets" +
                    "?vs_currency=usd" +
                    "&order=market_cap_desc" +
                    "&per_page=10" +
                    "&page=" + page +
                    "&sparkline=false";

            try {
                String body = callApi(url);
                List<Coin> coins = objectMapper.readValue(body, new TypeReference<List<Coin>>() {});

                for (Coin coin : coins) {
                    coinRepository.upsert(coin);
                }

                coinCacheService.saveCoinList(page, coins);
                return coins;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse coin list: " + e.getMessage());
            }
        }
    }

    @Override
    public String getMarketChart(String coinId, int days) {
        String url = baseUrl + "/coins/" + coinId +
                "/market_chart?vs_currency=usd&days=" + days;
        return callApi(url);
    }

    @Override
    public String getCoinDetail(String coinId) {
        String url = baseUrl + "/coins/" + coinId +
                "?localization=false&tickers=false&market_data=true" +
                "&community_data=false&developer_data=false&sparkline=false";
        return callApi(url);
    }

    @Override
    public Coin findCoinById(String coinId) {
        return coinRepository.findById(coinId)
                .orElseThrow(() -> new RuntimeException("Coin not found: " + coinId));
    }

    @Override
    public String searchCoin(String keyword) {
        String url = baseUrl + "/search?query=" + keyword;
        return callApi(url);
    }

    @Override
    public String getTop50CoinsByMarketCapRank() {
        String url = baseUrl + "/coins/markets" +
                "?vs_currency=usd" +
                "&order=market_cap_desc" +
                "&per_page=50" +
                "&page=1" +
                "&sparkline=false";
        return callApi(url);
    }

    @Override
    public String getTrendingCoins() {
        String url = baseUrl + "/search/trending";
        return callApi(url);
    }

    private static final Map<String, String> COIN_ID_TO_SYMBOL = Map.ofEntries(
            Map.entry("bitcoin",      "BTCUSDT"),
            Map.entry("ethereum",     "ETHUSDT"),
            Map.entry("binancecoin",  "BNBUSDT"),
            Map.entry("solana",       "SOLUSDT"),
            Map.entry("ripple",       "XRPUSDT"),
            Map.entry("cardano",      "ADAUSDT"),
            Map.entry("dogecoin",     "DOGEUSDT"),
            Map.entry("avalanche-2",  "AVAXUSDT"),
            Map.entry("polkadot",     "DOTUSDT"),
            Map.entry("matic-network","MATICUSDT")
    );

    private String callBinanceApi(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Binance API error: " + e.getMessage());
        }
    }

    @Override
    public List<KlineResponse> getKlines(String coinId, String interval, int limit) {
        String symbol = COIN_ID_TO_SYMBOL.get(coinId);
        if (symbol == null) throw new RuntimeException("Unsupported coin: " + coinId);

        int safeLimit = Math.min(limit, 1000);

        String url = "https://api.binance.com/api/v3/klines" +
                "?symbol=" + symbol +
                "&interval=" + interval +
                "&limit=" + safeLimit;

        try {
            String body = callBinanceApi(url);
            JsonNode root = objectMapper.readTree(body);
            List<KlineResponse> result = new ArrayList<>();

            for (JsonNode kline : root) {
                result.add(KlineResponse.builder()
                        .openTime(kline.get(0).asLong())
                        .open(kline.get(1).asText())
                        .high(kline.get(2).asText())
                        .low(kline.get(3).asText())
                        .close(kline.get(4).asText())
                        .volume(kline.get(5).asText())
                        .closeTime(kline.get(6).asLong())
                        .build());
            }

            log.info("[Binance] Fetched {} klines for {}/{}", result.size(), coinId, interval);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch klines for : " + coinId + ": " + e.getMessage());
        }
        return null;
    }
}