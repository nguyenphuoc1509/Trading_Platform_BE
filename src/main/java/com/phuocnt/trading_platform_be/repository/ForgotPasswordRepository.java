package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, String> {
    ForgotPassword findByUserId(Long userId);
}
