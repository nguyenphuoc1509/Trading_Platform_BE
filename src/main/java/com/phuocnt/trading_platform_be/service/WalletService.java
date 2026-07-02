package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.entity.Wallet;
import com.phuocnt.trading_platform_be.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    Wallet getWalletByUserId(Long userId);
    Wallet deposit(Long userId, BigDecimal amount);
    Wallet withdraw(Long userId, BigDecimal amount);
    Wallet lockBalance(Long userId, BigDecimal amount, String purpose);
    Wallet unlockBalance(Long userId, BigDecimal amount, String purpose);
    Wallet consumeLockedBalance(Long userId, BigDecimal amount, String purpose);
    List<WalletTransaction> getTransactionHistory(Long userId);
}
