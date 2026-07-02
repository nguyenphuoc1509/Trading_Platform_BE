package com.phuocnt.trading_platform_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletResponse {
    private Long walletId;
    private String balance;
    private String availableBalance;
    private String lockedBalance;
    private String totalBalance;
    private String currency;
}
