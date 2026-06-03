package com.phuocnt.trading_platform_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletResponse {
    private Long walletId;
    private String balance;
    private String currency;
}
