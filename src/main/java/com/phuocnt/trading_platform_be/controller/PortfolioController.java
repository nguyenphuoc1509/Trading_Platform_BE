package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.dto.response.ApiEnvelope;
import com.phuocnt.trading_platform_be.dto.response.PortfolioResponse;
import com.phuocnt.trading_platform_be.entity.Portfolio;
import com.phuocnt.trading_platform_be.entity.PortfolioItem;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.mapper.PortfolioMapper;
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

    // return portfolio + items + pnl
    @GetMapping
    public ResponseEntity<ApiEnvelope<PortfolioResponse>> getPortfolio(
            @AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        Portfolio portfolio = portfolioService.getPortfolio(user);
        List<PortfolioItem> items = portfolioItemRepository.findByPortfolio(portfolio);
        return ResponseEntity.ok(ApiEnvelope.success(
                PortfolioMapper.toPortfolioResponse(portfolio, items)));
    }
}
