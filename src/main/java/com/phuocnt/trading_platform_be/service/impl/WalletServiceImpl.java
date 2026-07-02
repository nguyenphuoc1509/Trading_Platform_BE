package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.entity.Wallet;
import com.phuocnt.trading_platform_be.entity.WalletTransaction;
import com.phuocnt.trading_platform_be.enums.TransactionStatus;
import com.phuocnt.trading_platform_be.enums.TransactionType;
import com.phuocnt.trading_platform_be.exception.BadRequestException;
import com.phuocnt.trading_platform_be.exception.InsufficientBalanceException;
import com.phuocnt.trading_platform_be.exception.NotFoundException;
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

    @Override
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> createWallet(userId));
    }

    private Wallet createWallet(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setAvailableBalance(BigDecimal.ZERO);
            wallet.setLockedBalance(BigDecimal.ZERO);
            return walletRepository.save(wallet);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("[Wallet] Race condition detected for userId {} - fetching existing wallet", userId);
            return walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new NotFoundException("Wallet not found after duplicate insert for userId: " + userId));
        }
    }

    @Override
    @Transactional
    public Wallet deposit(Long userId, BigDecimal amount) {
        validatePositiveAmount(amount, "Deposit amount must be positive");

        Wallet wallet = getWalletByUserIdForUpdateOrCreate(userId);
        wallet.creditAvailable(amount);
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, TransactionType.DEPOSIT, "Deposit");
        return wallet;
    }

    @Override
    @Transactional
    public Wallet withdraw(Long userId, BigDecimal amount) {
        validatePositiveAmount(amount, "Withdraw amount must be positive");

        Wallet wallet = getWalletByUserIdForUpdateOrCreate(userId);
        try {
            wallet.debitAvailable(amount);
        } catch (IllegalStateException e) {
            throw new InsufficientBalanceException("Insufficient available balance");
        }
        walletRepository.save(wallet);

        saveTransaction(wallet, amount.negate(), TransactionType.WITHDRAWAL, "Manual withdrawal");
        return wallet;
    }

    @Override
    @Transactional
    public Wallet lockBalance(Long userId, BigDecimal amount, String purpose) {
        validatePositiveAmount(amount, "Lock amount must be positive");

        Wallet wallet = getWalletByUserIdForUpdateOrCreate(userId);
        try {
            wallet.lock(amount);
        } catch (IllegalStateException e) {
            throw new InsufficientBalanceException("Insufficient available balance");
        }
        walletRepository.save(wallet);

        saveTransaction(wallet, amount.negate(), TransactionType.BUY, purpose);
        return wallet;
    }

    @Override
    @Transactional
    public Wallet unlockBalance(Long userId, BigDecimal amount, String purpose) {
        validatePositiveAmount(amount, "Unlock amount must be positive");

        Wallet wallet = getWalletByUserIdForUpdateOrCreate(userId);
        try {
            wallet.unlock(amount);
        } catch (IllegalStateException e) {
            throw new InsufficientBalanceException("Insufficient locked balance");
        }
        walletRepository.save(wallet);

        saveTransaction(wallet, amount, TransactionType.DEPOSIT, purpose);
        return wallet;
    }

    @Override
    @Transactional
    public Wallet consumeLockedBalance(Long userId, BigDecimal amount, String purpose) {
        validatePositiveAmount(amount, "Consume locked amount must be positive");

        Wallet wallet = getWalletByUserIdForUpdateOrCreate(userId);
        try {
            wallet.consumeLocked(amount);
        } catch (IllegalStateException e) {
            throw new InsufficientBalanceException("Insufficient locked balance");
        }
        walletRepository.save(wallet);

        saveTransaction(wallet, amount.negate(), TransactionType.BUY, purpose);
        return wallet;
    }

    @Override
    public List<WalletTransaction> getTransactionHistory(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet);
    }

    private Wallet getWalletByUserIdForUpdateOrCreate(Long userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> {
                    createWallet(userId);
                    return walletRepository.findByUserIdForUpdate(userId)
                            .orElseThrow(() -> new NotFoundException("Wallet not found for userId: " + userId));
                });
    }

    private void validatePositiveAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(message);
        }
    }

    private void saveTransaction(Wallet wallet, BigDecimal amount, TransactionType type, String purpose) {
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setPurpose(purpose);
        transactionRepository.save(tx);
    }
}
