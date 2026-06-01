package com.phuocnt.trading_platform_be.config;

import com.phuocnt.trading_platform_be.entity.Role;
import com.phuocnt.trading_platform_be.enums.RoleCode;
import com.phuocnt.trading_platform_be.repository.RoleRepository;
import com.phuocnt.trading_platform_be.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitialize implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CoinService coinService; // thêm

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initCoins(); // thêm
    }

    private void initRoles() {
        if (roleRepository.findByCode(RoleCode.ROLE_USER).isEmpty()) {
            Role userRole = Role.builder()
                    .code(RoleCode.ROLE_USER)
                    .name("User")
                    .build();
            roleRepository.save(userRole);
            System.out.println("Created ROLE_USER");
        }

        if (roleRepository.findByCode(RoleCode.ROLE_ADMIN).isEmpty()) {
            Role adminRole = Role.builder()
                    .code(RoleCode.ROLE_ADMIN)
                    .name("Admin")
                    .build();
            roleRepository.save(adminRole);
            System.out.println("Created ROLE_ADMIN");
        }
    }

    private void initCoins() {
        try {
            log.info("Syncing coins on startup...");
            for (int page = 1; page <= 3; page++) {
                coinService.getCoinsList(page);
                Thread.sleep(2000); // tránh rate limit CoinGecko
            }
            log.info("Coin sync completed — 30 coins loaded");
        } catch (Exception e) {
            log.warn("Coin sync failed: {} — app vẫn chạy bình thường", e.getMessage());
        }
    }
}