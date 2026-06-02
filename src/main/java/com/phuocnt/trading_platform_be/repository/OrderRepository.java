package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.Order;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.OrderMode;
import com.phuocnt.trading_platform_be.enums.OrderStatus;
import com.phuocnt.trading_platform_be.enums.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    //get limit order for scheduler
    List<Order> findByStatusAndMode(OrderStatus status, OrderMode mode);

    // sort by status and user
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);

    // sort by status, user and type (Buy/Sell)
    List<Order> findByUserAndStatusAndTypeOrderByCreatedAtDesc(User user, OrderStatus status, OrderType type);
}
