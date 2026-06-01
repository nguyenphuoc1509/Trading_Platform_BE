package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.entity.Portfolio;
import com.phuocnt.trading_platform_be.entity.PortfolioItem;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.repository.PortfolioItemRepository;
import com.phuocnt.trading_platform_be.repository.PortfolioRepository;
import com.phuocnt.trading_platform_be.service.CoinService;
import com.phuocnt.trading_platform_be.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final CoinService coinService;

    @Override
    public Portfolio getPortfolio(User user) {
        return portfolioRepository.findByUserId(user.getId()).orElseGet(() -> createPortfolio(user));
    }

    private Portfolio createPortfolio(User user) {
        Portfolio p = new Portfolio();
        p.setUser(user);
        return portfolioRepository.save(p);
    }

    @Override
    public void addCoinToPortfolio(User user, Coin coin, BigDecimal quantity, BigDecimal price) {
        Portfolio portfolio = getPortfolio(user);
        Optional<PortfolioItem> existingItem = portfolioItemRepository.findByPortfolioAndCoin(portfolio, coin);

        if (existingItem.isPresent()) {
            PortfolioItem item = existingItem.get();

            BigDecimal totalQty = item.getQuantity().add(quantity);
            BigDecimal newAvg = item.getAvgBuyPrice()
                    .multiply(item.getQuantity())
                    .add(price.multiply(quantity))
                    .divide(totalQty, 8, RoundingMode.HALF_UP);
            item.setQuantity(totalQty);
            item.setAvgBuyPrice(newAvg);
            portfolioRepository.save(portfolio);
        } else {
            PortfolioItem item = new PortfolioItem();
            item.setPortfolio(portfolio);
            item.setCoin(coin);
            item.setQuantity(quantity);
            item.setAvgBuyPrice(price);
            portfolioItemRepository.save(item);
        }
        updatePortfolioValue(portfolio);
    }

    @Override
    public void validateSellQuantity(User user, Coin coin, BigDecimal quantity) {
        Portfolio portfolio = getPortfolio(user);
        PortfolioItem item = portfolioItemRepository
                .findByPortfolioAndCoin(portfolio, coin)
                .orElseThrow(() -> new RuntimeException("You don't own any " + coin.getSymbol()));

        if (item.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("You don't have enough " + coin.getSymbol() + " to sell");
        }
    }

    @Override
    public void removeCoinFromPortfolio(User user, Coin coin, BigDecimal quantity) {
        Portfolio portfolio = getPortfolio(user);
        PortfolioItem item = portfolioItemRepository
                .findByPortfolioAndCoin(portfolio, coin)
                .orElseThrow(() -> new RuntimeException("Coin not found in portfolio"));

        BigDecimal remaining = item.getQuantity().subtract(quantity);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            portfolioItemRepository.delete(item);
        } else {
            item.setQuantity(remaining);
            portfolioRepository.save(portfolio);
        }
        updatePortfolioValue(portfolio);
    }

    private void updatePortfolioValue(Portfolio portfolio) {
        BigDecimal total = portfolioItemRepository.findByPortfolio(portfolio)
                .stream()
                .map(item -> BigDecimal.valueOf(item.getCoin().getCurrentPrice())
                        .multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        portfolio.setTotalValue(total);
        portfolio.setUpdatedAt(LocalDateTime.now());
        portfolioRepository.save(portfolio);
    }
}
