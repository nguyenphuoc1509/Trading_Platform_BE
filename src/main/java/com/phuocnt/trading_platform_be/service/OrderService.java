package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.entity.Order;
import com.phuocnt.trading_platform_be.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    Order buyOrder(User user, String coinId, BigDecimal quantity);
    Order sellOrder(User user, String coinId, BigDecimal quantity);
    List<Order> getOrdersByUser(User user);
}
