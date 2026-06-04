package com.phuocnt.trading_platform_be.scheduler;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.service.CoinService;
import com.phuocnt.trading_platform_be.service.PricePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoinSyncScheduler {

    private final CoinService coinService;
    private final PricePublisher pricePublisher;

    @Scheduled(fixedDelay = 900_000) // 15 phút
    public void syncCoins() {
        try {
            List<Coin> allSyncedCoins = new ArrayList<>();
            for (int page = 0; page <= 3; page++) {
                List<Coin> coins = coinService.getCoinsList(page);
                allSyncedCoins.addAll(coins);
                Thread.sleep(3000);
            }
            log.info("Coin sync completed — {} coins synced", allSyncedCoins.size());

            pricePublisher.broadcastPrices(allSyncedCoins);
        } catch (Exception e) {
            log.error("Coin sync failed: {}", e.getMessage());
        }
    }
}
