package com.phuocnt.trading_platform_be.dto.response;

import com.phuocnt.trading_platform_be.enums.OrderMode;
import com.phuocnt.trading_platform_be.enums.OrderStatus;
import com.phuocnt.trading_platform_be.enums.OrderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private String orderId;
    private String symbol;
    private String coinName;
    private String coinImage;
    private String quantity;
    private String price;
    private String executedPrice;
    private String totalValue;
    private OrderType side;
    private OrderMode orderType;
    private OrderStatus status;
    private Long createdAt;
    private Long executedAt;
}
