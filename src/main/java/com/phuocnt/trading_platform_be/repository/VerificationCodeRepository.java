package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    VerificationCode findByUserId(Long userId);

}
