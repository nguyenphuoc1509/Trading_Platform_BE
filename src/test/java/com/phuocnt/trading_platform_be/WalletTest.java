package com.phuocnt.trading_platform_be;

import com.phuocnt.trading_platform_be.entity.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    @Test
    void lock_movesAvailableToLocked() {
        Wallet wallet = walletWithAvailable("100.0000");

        wallet.lock(new BigDecimal("40.0000"));

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("60.0000");
        assertThat(wallet.getLockedBalance()).isEqualByComparingTo("40.0000");
        assertThat(wallet.getTotalBalance()).isEqualByComparingTo("100.0000");
        assertThat(wallet.getBalance()).isEqualByComparingTo("100.0000");
    }

    @Test
    void unlock_movesLockedToAvailable() {
        Wallet wallet = walletWithAvailable("100.0000");
        wallet.lock(new BigDecimal("40.0000"));

        wallet.unlock(new BigDecimal("15.0000"));

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("75.0000");
        assertThat(wallet.getLockedBalance()).isEqualByComparingTo("25.0000");
        assertThat(wallet.getTotalBalance()).isEqualByComparingTo("100.0000");
    }

    @Test
    void consumeLocked_doesNotDebitAvailableAgain() {
        Wallet wallet = walletWithAvailable("100.0000");
        wallet.lock(new BigDecimal("40.0000"));

        wallet.consumeLocked(new BigDecimal("40.0000"));

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("60.0000");
        assertThat(wallet.getLockedBalance()).isEqualByComparingTo("0.0000");
        assertThat(wallet.getTotalBalance()).isEqualByComparingTo("60.0000");
    }

    @Test
    void debitAvailable_cannotUseLockedBalance() {
        Wallet wallet = walletWithAvailable("100.0000");
        wallet.lock(new BigDecimal("80.0000"));

        assertThatThrownBy(() -> wallet.debitAvailable(new BigDecimal("30.0000")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient available balance");
    }

    private Wallet walletWithAvailable(String amount) {
        Wallet wallet = new Wallet();
        wallet.setAvailableBalance(new BigDecimal(amount));
        wallet.setLockedBalance(BigDecimal.ZERO);
        wallet.setBalance(new BigDecimal(amount));
        return wallet;
    }
}
