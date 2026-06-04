package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.service.CoinService;
import com.phuocnt.trading_platform_be.service.PricePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PriceWebSocketController {

    private final CoinService coinService;
    private final PricePublisher pricePublisher;

    @MessageMapping("/subscribe/{coinId}")
    public void subscribeCoin(@DestinationVariable String coinId) {
        log.info("[WebSocket] Client subscribed to coin: {}", coinId);
        try {
            Coin coin = coinService.findCoinById(coinId);
            pricePublisher.broadcastSinglePrice(coin);
        } catch (Exception e) {
            log.warn("[WebSocket] Failed to subscribe to coin: {}", e.getMessage());
        }
    }
}
