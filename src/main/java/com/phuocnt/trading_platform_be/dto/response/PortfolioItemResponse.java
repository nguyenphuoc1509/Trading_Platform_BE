package com.phuocnt.trading_platform_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioItemResponse {
    private Long itemId;
    private CoinSummaryResponse coin;
    private String quantity;
    private String avgBuyPrice;
    private String currentPrice;
    private String currentValue;
    private String pnl;
    private String pnlPercentage;
    private boolean isProfitable;
}
