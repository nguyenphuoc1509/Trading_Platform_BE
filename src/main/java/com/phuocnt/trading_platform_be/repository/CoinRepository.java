package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.Coin;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoinRepository extends JpaRepository<Coin, String> {
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO coin (id, symbol, name, image, current_price, market_cap,
            market_cap_rank, fully_diluted_valuation, total_volume, high24h, low24h,
            price_change24h, price_change_percentage24h, market_cap_change24h,
            market_cap_change_percentage24h, circulating_supply, total_supply,
            max_supply, ath, ath_change_percentage, ath_date, atl,
            atl_change_percentage, atl_date, last_updated)
        VALUES (:#{#c.id}, :#{#c.symbol}, :#{#c.name}, :#{#c.image},
            :#{#c.currentPrice}, :#{#c.marketCap}, :#{#c.marketCapRank},
            :#{#c.fullyDilutedValuation}, :#{#c.totalVolume}, :#{#c.high24h},
            :#{#c.low24h}, :#{#c.priceChange24h}, :#{#c.priceChangePercentage24h},
            :#{#c.marketCapChange24h}, :#{#c.marketCapChangePercentage24h},
            :#{#c.circulatingSupply}, :#{#c.totalSupply}, :#{#c.maxSupply},
            :#{#c.ath}, :#{#c.athChangePercentage}, :#{#c.athDate},
            :#{#c.atl}, :#{#c.atlChangePercentage}, :#{#c.atlDate}, :#{#c.lastUpdated})
        ON DUPLICATE KEY UPDATE
            current_price = VALUES(current_price),
            market_cap = VALUES(market_cap),
            market_cap_rank = VALUES(market_cap_rank),
            total_volume = VALUES(total_volume),
            high24h = VALUES(high24h),
            low24h = VALUES(low24h),
            price_change24h = VALUES(price_change24h),
            price_change_percentage24h = VALUES(price_change_percentage24h),
            last_updated = VALUES(last_updated)
        """, nativeQuery = true)
    void upsert(@Param("c") Coin coin);
}
