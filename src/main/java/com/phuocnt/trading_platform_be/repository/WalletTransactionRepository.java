package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.Wallet;
import com.phuocnt.trading_platform_be.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);
}
