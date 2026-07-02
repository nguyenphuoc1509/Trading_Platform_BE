package com.phuocnt.trading_platform_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(precision = 19, scale = 4)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal lockedBalance = BigDecimal.ZERO;

    private String currency = "USD";

    @Version
    private Long version;

    public BigDecimal getTotalBalance() {
        normalizeBalances();
        return availableBalance.add(lockedBalance);
    }

    public void creditAvailable(BigDecimal amount) {
        validatePositiveAmount(amount);
        normalizeBalances();
        availableBalance = availableBalance.add(amount);
        syncLegacyBalance();
    }

    public void debitAvailable(BigDecimal amount) {
        validatePositiveAmount(amount);
        normalizeBalances();
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance");
        }
        availableBalance = availableBalance.subtract(amount);
        syncLegacyBalance();
    }

    public void lock(BigDecimal amount) {
        validatePositiveAmount(amount);
        normalizeBalances();
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance");
        }
        availableBalance = availableBalance.subtract(amount);
        lockedBalance = lockedBalance.add(amount);
        syncLegacyBalance();
    }

    public void unlock(BigDecimal amount) {
        validatePositiveAmount(amount);
        normalizeBalances();
        if (lockedBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient locked balance");
        }
        lockedBalance = lockedBalance.subtract(amount);
        availableBalance = availableBalance.add(amount);
        syncLegacyBalance();
    }

    public void consumeLocked(BigDecimal amount) {
        validatePositiveAmount(amount);
        normalizeBalances();
        if (lockedBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient locked balance");
        }
        lockedBalance = lockedBalance.subtract(amount);
        syncLegacyBalance();
    }

    @PrePersist
    @PreUpdate
    private void syncBeforeSave() {
        normalizeBalances();
        syncLegacyBalance();
    }

    private void normalizeBalances() {
        if (availableBalance == null) {
            availableBalance = balance == null ? BigDecimal.ZERO : balance;
        }
        if (lockedBalance == null) {
            lockedBalance = BigDecimal.ZERO;
        }
        if (balance == null) {
            syncLegacyBalance();
        }
    }

    private void syncLegacyBalance() {
        balance = availableBalance.add(lockedBalance);
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
