package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.entity.Portfolio;
import com.phuocnt.trading_platform_be.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    Optional<PortfolioItem> findByPortfolioAndCoin(Portfolio portfolio, Coin coin);
    List<PortfolioItem> findByPortfolio(Portfolio portfolio);
}