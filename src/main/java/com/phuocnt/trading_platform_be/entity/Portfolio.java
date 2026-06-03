package com.phuocnt.trading_platform_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.now();
}
