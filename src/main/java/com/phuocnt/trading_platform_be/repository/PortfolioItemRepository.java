package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.entity.Portfolio;
import com.phuocnt.trading_platform_be.entity.PortfolioItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    Optional<PortfolioItem> findByPortfolioAndCoin(Portfolio portfolio, Coin coin);
    List<PortfolioItem> findByPortfolio(Portfolio portfolio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pi
            from PortfolioItem pi
            where pi.portfolio = :portfolio
              and pi.coin = :coin
            """)
    Optional<PortfolioItem> findByPortfolioAndCoinForUpdate(
            @Param("portfolio") Portfolio portfolio,
            @Param("coin") Coin coin
    );
}