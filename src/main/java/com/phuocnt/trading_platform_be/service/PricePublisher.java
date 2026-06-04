package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.dto.ws.PriceMessage;
import com.phuocnt.trading_platform_be.entity.Coin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.phuocnt.trading_platform_be.mapper.MapperUtils.fmtDouble4;
import static com.phuocnt.trading_platform_be.mapper.MapperUtils.fmtDouble8;


@Service
@RequiredArgsConstructor
@Slf4j
public class PricePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastPrices(List<Coin> coins) {
        if (coins == null || coins.isEmpty()) return;

        long timestamp = System.currentTimeMillis();

        for (Coin coin : coins) {
            PriceMessage msg = buildMessage(coin, timestamp);
            messagingTemplate.convertAndSend("/topic/prices", msg);
            messagingTemplate.convertAndSend("/topic/prices/" + coin.getId(), msg);
        }
    }

    public void broadcastSinglePrice(Coin coin) {
        if (coin == null) return;
        long timestamp = System.currentTimeMillis();
        PriceMessage msg = buildMessage(coin, timestamp);
        messagingTemplate.convertAndSend("/topic/prices", msg);
        messagingTemplate.convertAndSend("/topic/prices/" + coin.getId(), msg);
    }

    private PriceMessage buildMessage(Coin coin, long timestamp) {
        return PriceMessage.builder()
                .type("PRICE_UPDATE")
                .coinId(coin.getId())
                .symbol(coin.getSymbol() != null ? coin.getSymbol().toUpperCase() : "")
                .name(coin.getName())
                .price(fmtDouble8(coin.getCurrentPrice()))
                .change24h(fmtDouble4(coin.getPriceChangePercentage24h()))
                .timestamp(timestamp)
                .build();
    }
}
