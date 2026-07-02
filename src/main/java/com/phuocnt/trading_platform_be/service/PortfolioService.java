package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.entity.Coin;
import com.phuocnt.trading_platform_be.entity.Portfolio;
import com.phuocnt.trading_platform_be.entity.User;

import java.math.BigDecimal;

public interface PortfolioService {
    Portfolio getPortfolio(User user);
    void addCoinToPortfolio(User user, Coin coin, BigDecimal quantity, BigDecimal price);
    void validateSellQuantity(User user, Coin coin, BigDecimal quantity);
    void removeCoinFromPortfolio(User user, Coin coin, BigDecimal quantity);
    void restoreCoinToPortfolio(User user, Coin coin, BigDecimal quantity);
}
