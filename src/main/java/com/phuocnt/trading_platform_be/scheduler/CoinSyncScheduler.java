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

    @Scheduled(fixedDelay = 300_000)
    public void syncCoins() {
        try {
            for (int page = 1; page <= 10; page++) {
                coinService.getCoinsList(page);
                Thread.sleep(2000);
            }
            log.info("Coin sync completed");
        } catch (Exception e) {
            log.error("Coin sync failed: {}", e.getMessage());
        }
    }
}
