package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinRepository extends JpaRepository<Coin, String> {
}
