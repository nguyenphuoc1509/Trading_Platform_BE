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

    @Scheduled(initialDelay = 0, fixedDelay = 86_400_000) // 15 phút
    public void syncCoinMetadata() {
        try {
            log.info("[Scheduler] Starting coin metadata sync from CoinGecko...");

            coinCacheService.invalidateCoinList();

            int totalSynced = 0;
            for (int page = 1; page <= 3; page++) {
                coinService.getCoinsList(page);
                totalSynced += 10;
                Thread.sleep(3000);
            }

            log.info("[Scheduler] Coin metadata sync completed — {} coins synced to DB", totalSynced);
        } catch (Exception e) {
            log.error("[Scheduler] Coin metadata sync failed: {}", e.getMessage());
        }
    }
}
