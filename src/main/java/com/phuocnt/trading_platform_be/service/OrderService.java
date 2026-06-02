package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.dto.request.OrderRequest;
import com.phuocnt.trading_platform_be.entity.Order;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.OrderType;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    // entry point for orders modes (MARKET, LIMIT)
    Order placeOrder(User user, OrderRequest req, OrderType type);

    // cancle limit order (PENDING)
    Order cancelOrder(User user, Long orderId);

    // get orders history
    List<Order> getOrdersByUser(User user);

    // get limit orders
    List<Order> getPendingOrders(User user);

    void executePendingLimitOrder();
}
