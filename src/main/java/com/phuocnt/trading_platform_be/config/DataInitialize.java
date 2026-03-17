package com.phuocnt.trading_platform_be.config;

import com.phuocnt.trading_platform_be.entity.Role;
import com.phuocnt.trading_platform_be.enums.RoleCode;
import com.phuocnt.trading_platform_be.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitialize implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize ROLE_USER if not exists
        if (roleRepository.findByCode(RoleCode.ROLE_USER).isEmpty()) {
            Role userRole = Role.builder()
                    .code(RoleCode.ROLE_USER)
                    .name("User")
                    .build();
            roleRepository.save(userRole);
            System.out.println("Created ROLE_USER");
        }

        // Initialize ROLE_ADMIN if not exists
        if (roleRepository.findByCode(RoleCode.ROLE_ADMIN).isEmpty()) {
            Role adminRole = Role.builder()
                    .code(RoleCode.ROLE_ADMIN)
                    .name("Admin")
                    .build();
            roleRepository.save(adminRole);
            System.out.println("Created ROLE_ADMIN");
        }
    }
}
