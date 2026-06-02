package com.phuocnt.trading_platform_be.scheduler;

import com.phuocnt.trading_platform_be.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoinSyncScheduler {

    private final CoinService coinService;

    @Scheduled(fixedDelay = 900_000) // 15 phút
    public void syncCoins() {
        try {
            for (int page = 1; page <= 3; page++) {
                coinService.getCoinsList(page);
                Thread.sleep(3000);
            }
            log.info("Coin sync completed — {} coins synced", 3 * 10);
        } catch (Exception e) {
            log.error("Coin sync failed: {}", e.getMessage());
        }
    }
}
