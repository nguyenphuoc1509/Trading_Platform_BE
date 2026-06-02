package com.phuocnt.trading_platform_be.scheduler;

import com.phuocnt.trading_platform_be.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LimitOrderScheduler {

    private final OrderService orderService;

    //fixedDelay: 30s after the last execution
    @Scheduled(fixedDelay = 30_000)
    public void checkAndExecuteLimitOrders() {
        log.debug("LimitOrderScheduler running...");
        orderService.executePendingLimitOrder();
    }

}
