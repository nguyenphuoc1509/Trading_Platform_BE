package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.dto.response.ApiEnvelope;
import com.phuocnt.trading_platform_be.dto.response.WalletResponse;
import com.phuocnt.trading_platform_be.dto.response.WalletTransactionResponse;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.entity.Wallet;
import com.phuocnt.trading_platform_be.entity.WalletTransaction;
import com.phuocnt.trading_platform_be.mapper.WalletMapper;
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
    public ResponseEntity<ApiEnvelope<WalletResponse>> getWallet(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(WalletMapper.toWalletResponse(walletService.getWalletByUserId(user.getId()))));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiEnvelope> deposit(@AuthenticationPrincipal Jwt jwt,@RequestParam BigDecimal amount) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(walletService.deposit(user.getId(), amount)));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiEnvelope<WalletResponse>> withdraw(@AuthenticationPrincipal Jwt jwt,@RequestParam BigDecimal amount) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(WalletMapper.toWalletResponse(walletService.withdraw(user.getId(), amount))));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiEnvelope<List<WalletTransactionResponse>>> getTransactionsHistory(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(WalletMapper.toTxResponseList(walletService.getTransactionHistory(user.getId()))));
    }
}
