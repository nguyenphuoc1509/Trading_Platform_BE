package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.dto.request.OrderRequest;
import com.phuocnt.trading_platform_be.entity.*;
import com.phuocnt.trading_platform_be.enums.*;
import com.phuocnt.trading_platform_be.repository.OrderRepository;
import com.phuocnt.trading_platform_be.repository.WalletRepository;
import com.phuocnt.trading_platform_be.repository.WalletTransactionRepository;
import com.phuocnt.trading_platform_be.service.CoinService;
import com.phuocnt.trading_platform_be.service.OrderService;
import com.phuocnt.trading_platform_be.service.PortfolioService;
import com.phuocnt.trading_platform_be.service.WalletService;
import com.phuocnt.trading_platform_be.service.CoinCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
    private final WalletRepository walletRepository;
    private final CoinCacheService coinCacheService;

    private void validateOrderRequest(OrderRequest req) {
        if (req.getMode() == OrderMode.LIMIT) {
            if (req.getLimitPrice() == null)
                throw new RuntimeException("Limit price is required for LIMIT orders");
            if (req.getLimitPrice().compareTo(BigDecimal.ZERO) <= 0)
                throw new RuntimeException("Limit price must be positive");
        }
    }

    private void deductFromWallet(User user, BigDecimal amount,
                                  String purpose, TransactionType txType) {
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0)
            throw new RuntimeException(
                    "Insufficient balance. Need: $" + amount
                            + ", Have: $" + wallet.getBalance());

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        saveWalletTransaction(wallet, amount.negate(), txType,
               TransactionStatus.SUCCESS, purpose);
    }

    // MARKET ORDER
    private Order executeMarketOrder(User user, Coin coin,
                                     BigDecimal quantity, OrderType type) {
        BigDecimal currentPrice = getRealtimePrice(coin);
        BigDecimal totalValue = currentPrice.multiply(quantity);

        if (type == OrderType.BUY) {
            deductFromWallet(user, totalValue,
                    "Buy " + formatQty(quantity) + " " + coin.getSymbol().toUpperCase(),
                    TransactionType.BUY);
            portfolioService.addCoinToPortfolio(user, coin, quantity, currentPrice);
        } else {
            portfolioService.validateSellQuantity(user, coin, quantity);
            addToBalance(user, totalValue,
                    "Sell " + quantity + " " + coin.getSymbol().toUpperCase(),
                    TransactionType.SELL);
            portfolioService.removeCoinFromPortfolio(user, coin, quantity);
        }

        Order order = new Order();
        order.setUser(user);
        order.setCoin(coin);
        order.setQuantity(quantity);
        order.setPrice(currentPrice);
        order.setExecutedPrice(currentPrice);
        order.setType(type);
        order.setMode(OrderMode.MARKET);
        order.setStatus(OrderStatus.SUCCESS);
        order.setExecutedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // LIMIT ORDER
    private Order placeLimitOrder(User user, Coin coin, BigDecimal quantity, BigDecimal limitPrice, OrderType type) {
        if (limitPrice == null || limitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Limit price must be positive");

        BigDecimal totalValue = limitPrice.multiply(quantity);

        if (type == OrderType.BUY) {
            deductFromWallet(user, totalValue,
                    "Lock for limit buy " + coin.getSymbol().toUpperCase(),
                    TransactionType.BUY);
        } else {
            portfolioService.validateSellQuantity(user, coin, quantity);
        }
        return saveOrder(user, coin, quantity, limitPrice, BigDecimal.ZERO, type, OrderStatus.PENDING, OrderMode.LIMIT);
    }

    // ENTRY POINT
    @Override
    @Transactional
    public Order placeOrder(User user, OrderRequest req, OrderType type) {
        validateOrderRequest(req);
        Coin coin = coinService.findCoinById(req.getCoinId());

        if (req.getMode() == OrderMode.MARKET) {
            return executeMarketOrder(user, coin, req.getQuantity(), type);
        } else {
            return placeLimitOrder(user, coin, req.getQuantity(),
                    req.getLimitPrice(), type);
        }
    }


    // CANCELL ORDER
    @Override
    public Order cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId()))
            throw new RuntimeException("You are not authorized to cancel this order");

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("cannot cancel order with status " + order.getStatus());
        }

        // refund money has been locked if Buy limit order
        if (order.getType() == OrderType.BUY && order.getMode() == OrderMode.LIMIT) {
            BigDecimal refund = order.getPrice().multiply(order.getQuantity())
                    .setScale(4, RoundingMode.HALF_UP);
            addToWallet(user, refund, "Cancel BUY limit order #" + orderId, TransactionType.DEPOSIT);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Order> getPendingOrders(User user) {
        return orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, OrderStatus.PENDING);
    }

    private void addToWallet(User user, BigDecimal amount, String purpose, TransactionType txType) {
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for user " + user.getId()));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    private void saveWalletTransaction(Wallet wallet, BigDecimal amount, TransactionType txType,
                                       TransactionStatus status, String purpose) {
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(txType);
        tx.setStatus(status);
        tx.setPurpose(purpose);
        walletTransactionRepository.save(tx);
    }

    private Order saveOrder(User user, Coin coin, BigDecimal quantity,
                           BigDecimal price, BigDecimal executedPrice,
                           OrderType type, OrderStatus status, OrderMode mode) {
        Order order = new Order();
        order.setUser(user);
        order.setCoin(coin);
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setExecutedPrice(executedPrice);
        order.setType(type);
        order.setStatus(status);
        order.setMode(mode);

        if (status == OrderStatus.SUCCESS) {
            order.setExecutedAt(LocalDateTime.now());
        }
        return orderRepository.save(order);
    }

    private void markOrderFailed(Order order, String reason) {
        try {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            log.warn("Order {} marked as FAILED: {}", order.getId(), reason);
        } catch (Exception e) {
            log.error("Failed to mark order {} as FAILED: {}", order.getId(), e.getMessage());
        }
    }

    private void executeBuyLimitOrder(Order order, BigDecimal executedPrice) {
        User user = order.getUser();
        Coin coin = order.getCoin();
        BigDecimal quantity = order.getQuantity();

        BigDecimal lockedAmount = order.getPrice().multiply(quantity).setScale(4, RoundingMode.HALF_UP);
        BigDecimal actualCost = executedPrice.multiply(quantity).setScale(4, RoundingMode.HALF_UP);

        BigDecimal refund = lockedAmount.subtract(actualCost);
        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            addToWallet(user, refund, "Refund from BUY limit " + coin.getSymbol().toUpperCase(),
                    TransactionType.DEPOSIT);
            log.info("Refunded {} to user {} from BUY limit order {}", refund, user.getId(),order.getId());
        }

        // add coint to portfolio
        portfolioService.addCoinToPortfolio(user, coin, quantity, executedPrice);

        // update order
        order.setExecutedPrice(executedPrice);
        order.setStatus(OrderStatus.SUCCESS);
        order.setExecutedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private void executeSellLimitOrder(Order order, BigDecimal executedPrice) {
        User user = order.getUser();
        Coin coin = order.getCoin();
        BigDecimal quantity = order.getQuantity();

        // remove coin from portfolio
        portfolioService.removeCoinFromPortfolio(user, coin, quantity);

        // add money to wallet
        BigDecimal revenue = executedPrice.multiply(quantity)
                .setScale(4, RoundingMode.HALF_UP);
        addToWallet(user, revenue, "SELL limit " + formatQty(quantity) + " " + coin.getSymbol().toUpperCase(),
                TransactionType.SELL);

        //update order
        order.setExecutedPrice(executedPrice);
        order.setStatus(OrderStatus.SUCCESS);
        order.setExecutedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    protected void processSinglePendingOrder(Order order) {
        Coin coin = coinService.findCoinById(order.getCoin().getId());
        BigDecimal currentPrice = getRealtimePrice(coin);

        boolean shouldExecute = checkPriceCondition(order, currentPrice);

        if (!shouldExecute) return;

        log.info("Executing order {} at price {}", order.getId(), currentPrice);

        if (order.getType() == OrderType.BUY) {
            executeBuyLimitOrder(order, currentPrice);
        } else {
            executeSellLimitOrder(order, currentPrice);
        }
    }

    private boolean checkPriceCondition(Order order, BigDecimal currentPrice) {
        return order.getType() == OrderType.BUY && currentPrice.compareTo(order.getPrice()) <= 0 ||
               order.getType() == OrderType.SELL && currentPrice.compareTo(order.getPrice()) >= 0;
    }

    // EXECUTE LIMIT ORDER EVERY 30 SECONDS
    @Override
    @Transactional
    public void executePendingLimitOrder() {
        List<Order> pendingOrders = orderRepository.findByStatusAndMode(OrderStatus.PENDING, OrderMode.LIMIT);

        if (pendingOrders.isEmpty()) return;

        log.info("Checking {} pending limit orders...", pendingOrders.size());

        for (Order order : pendingOrders) {
            try {
                processSinglePendingOrder(order);
            } catch (Exception e) {
                log.error("Error processing order {}: {}", order.getId(), e.getMessage());
                markOrderFailed(order, e.getMessage());
            }
        }

    }

    private void addToBalance(User user, BigDecimal amount,
                              String purpose, TransactionType txType) {
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(txType);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setPurpose(purpose);
        walletTransactionRepository.save(tx);
    }

    private String formatQty(BigDecimal qty) {
        return qty.stripTrailingZeros().toPlainString();
    }

    private BigDecimal getRealtimePrice(Coin coin) {
        return coinCacheService.getPrice(coin.getId())
                .map(BigDecimal::new)
                .orElseGet(() -> {
                    log.warn("[Order] Redis price unavailable for {} — using DB price (may be stale)",
                            coin.getId());
                    return BigDecimal.valueOf(coin.getCurrentPrice());
                });
    }
}
