package com.phuocnt.trading_platform_be.entity;

import com.phuocnt.trading_platform_be.enums.OrderMode;
import com.phuocnt.trading_platform_be.enums.OrderStatus;
import com.phuocnt.trading_platform_be.enums.OrderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @Column(precision = 19, scale = 8, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(precision = 19, scale = 4)
    private BigDecimal executedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, nullable = false)
    private OrderType type;   // BUY, SELL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status; // PENDING, SUCCESS, FAILED

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", length = 10, nullable = false)
    private OrderMode mode; // PENDING, SUCCESS, FAILED

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime executedAt;
}
