package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.Role;
import com.phuocnt.trading_platform_be.enums.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(RoleCode code);
}


