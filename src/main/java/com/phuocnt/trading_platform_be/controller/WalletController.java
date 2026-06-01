package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.entity.Wallet;
import com.phuocnt.trading_platform_be.entity.WalletTransaction;
import com.phuocnt.trading_platform_be.service.UserService;
import com.phuocnt.trading_platform_be.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Wallet> getWallet(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(walletService.getWalletByUserId(user.getId()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Wallet> deposit(@AuthenticationPrincipal Jwt jwt,@RequestParam BigDecimal amount) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(walletService.deposit(user.getId(), amount));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Wallet> withdraw(@AuthenticationPrincipal Jwt jwt,@RequestParam BigDecimal amount) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(walletService.withdraw(user.getId(), amount));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransaction>> getTransactionsHistory(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(walletService.getTransactionHistory(user.getId()));
    }
}
