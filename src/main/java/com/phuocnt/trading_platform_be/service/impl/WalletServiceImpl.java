package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.entity.Wallet;
import com.phuocnt.trading_platform_be.entity.WalletTransaction;
import com.phuocnt.trading_platform_be.enums.TransactionStatus;
import com.phuocnt.trading_platform_be.enums.TransactionType;
import com.phuocnt.trading_platform_be.repository.UserRepository;
import com.phuocnt.trading_platform_be.repository.WalletRepository;
import com.phuocnt.trading_platform_be.repository.WalletTransactionRepository;
import com.phuocnt.trading_platform_be.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private Wallet createWallet(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> createWallet(userId));
    }

    @Override
    @Transactional
    public Wallet deposit(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }

        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // Update wallet balance
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(TransactionType.DEPOSIT);
        tx.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(tx);
        return wallet;
    }

    @Override
    @Transactional
    public Wallet withdraw(Long userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(amount.negate()); // for "-amount"
        tx.setType(TransactionType.WITHDRAWAL);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setPurpose("Manual withdrawal");
        transactionRepository.save(tx);

        return wallet;
    }

    @Override
    public List<WalletTransaction> getTransactionHistory(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet);
    }
}
