package com.phuocnt.trading_platform_be.dto.request;

import com.phuocnt.trading_platform_be.enums.OrderMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {

    @NotBlank(message = "coinId is required")
    private String coinId;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "mode is required")
    private OrderMode mode;

    private BigDecimal limitPrice;
}
