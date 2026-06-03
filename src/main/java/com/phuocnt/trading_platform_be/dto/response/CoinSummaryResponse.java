package com.phuocnt.trading_platform_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoinSummaryResponse {
    private String id;
    private String symbol;
    private String name;
    private String image;
    private String currentPrice;
    private String priceChange24h;
    private Integer marketCapRank;
}
