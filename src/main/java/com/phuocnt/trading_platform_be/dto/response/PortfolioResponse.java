package com.phuocnt.trading_platform_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PortfolioResponse {
    private String totalValue;
    private String totalCost;
    private String totalPnl;
    private String totalPnlPercentage;
    private Long updatedAt;
    private List<PortfolioItemResponse> items;
}
