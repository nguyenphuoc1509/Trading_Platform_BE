package com.phuocnt.trading_platform_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItem {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    @Column(precision = 19, scale = 8)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal avgBuyPrice = BigDecimal.ZERO;
}
