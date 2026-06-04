package com.phuocnt.trading_platform_be.scheduler;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.service.CoinCacheService;
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
    private final CoinCacheService coinCacheService;

    @Scheduled(fixedDelay = 900_000) // 15 phút
    public void syncCoins() {
        try {
            int totalSynched = 0;
            for (int page = 1; page <= 3; page++) {
                coinService.getCoinsList(page);
                totalSynched += 10;
                Thread.sleep(3000);
            }
            coinCacheService.invalidateCoinList();
            log.info("[Scheduler] Coin sync completed - {} coins synced to DB", totalSynched);
        } catch (Exception e) {
            log.error("[Scheduler] Coin sync failed: {}", e.getMessage());
        }
    }
}
