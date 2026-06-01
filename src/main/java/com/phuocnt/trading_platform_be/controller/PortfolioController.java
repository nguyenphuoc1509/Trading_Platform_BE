package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.entity.Portfolio;
import com.phuocnt.trading_platform_be.entity.PortfolioItem;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.repository.PortfolioItemRepository;
import com.phuocnt.trading_platform_be.service.PortfolioService;
import com.phuocnt.trading_platform_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PortfolioItemRepository portfolioItemRepository;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Portfolio> getPortfolio(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(portfolioService.getPortfolio(user));
    }

    @GetMapping("/items")
    public ResponseEntity<List<PortfolioItem>> getPortfolioItems(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        Portfolio portfolio = portfolioService.getPortfolio(user);
        return ResponseEntity.ok(portfolioItemRepository.findByPortfolio(portfolio));
    }
}
