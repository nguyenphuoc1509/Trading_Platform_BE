package com.phuocnt.trading_platform_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradingPlatformBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingPlatformBeApplication.class, args);
    }

}
