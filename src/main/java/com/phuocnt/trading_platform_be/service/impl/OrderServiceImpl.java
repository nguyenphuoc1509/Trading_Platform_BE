package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.*;
import com.phuocnt.trading_platform_be.enums.OrderStatus;
import com.phuocnt.trading_platform_be.enums.OrderType;
import com.phuocnt.trading_platform_be.enums.TransactionStatus;
import com.phuocnt.trading_platform_be.enums.TransactionType;
import com.phuocnt.trading_platform_be.repository.OrderRepository;
import com.phuocnt.trading_platform_be.repository.WalletTransactionRepository;
import com.phuocnt.trading_platform_be.service.CoinService;
import com.phuocnt.trading_platform_be.service.OrderService;
import com.phuocnt.trading_platform_be.service.PortfolioService;
import com.phuocnt.trading_platform_be.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final WalletService walletService;
    private final CoinService coinService;
    private final OrderRepository orderRepository;
    private final PortfolioService portfolioService;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    @Transactional
    public Order buyOrder(User user, String coinId, BigDecimal quantity) {
        Coin coin = coinService.findCoinById(coinId);
        BigDecimal currentPrice = BigDecimal.valueOf(coin.getCurrentPrice());
        BigDecimal totalCost = currentPrice.multiply(quantity);

        Wallet wallet = walletService.getWalletByUserId(user.getId());

        //check balance
        if (wallet.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient balance. Need " + totalCost + " but only have " + wallet.getBalance() + ".");
        }

        // deduct money from wallet
        wallet.setBalance(wallet.getBalance().subtract(totalCost));

        //set transaction
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(totalCost.negate());
        tx.setType(TransactionType.BUY);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setPurpose("Buy " + quantity + " " + coin.getSymbol().toUpperCase());
        walletTransactionRepository.save(tx);

        //set order
        Order order = new Order();
        order.setUser(user);
        order.setCoin(coin);
        order.setQuantity(quantity);
        order.setPrice(currentPrice);
        order.setType(OrderType.BUY);
        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);

        //update portfolio
        portfolioService.addCoinToPortfolio(user, coin, quantity, currentPrice);

        return order;
        }

    @Override
    public Order sellOrder(User user, String coinId, BigDecimal quantity) {
        Coin coin = coinService.findCoinById(coinId);
        BigDecimal currentPrice = BigDecimal.valueOf(coin.getCurrentPrice());
        BigDecimal totalRevenue = currentPrice.multiply(quantity);

        // Check coin in portfolio
        portfolioService.validateSellQuantity(user, coin, quantity);

        // add money to wallet
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        wallet.setBalance(wallet.getBalance().add(totalRevenue));

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(totalRevenue);
        tx.setType(TransactionType.SELL);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setPurpose("Sell " + quantity + " " + coin.getSymbol().toUpperCase());
        walletTransactionRepository.save(tx);

        Order order = new Order();
        order.setUser(user);
        order.setCoin(coin);
        order.setQuantity(quantity);
        order.setPrice(currentPrice);
        order.setType(OrderType.SELL);
        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);

        // remove coin from portfolio
        portfolioService.removeCoinFromPortfolio(user, coin, quantity);

        return order;
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
