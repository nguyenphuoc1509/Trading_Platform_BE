package com.phuocnt.trading_platform_be.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceMessage {
    private String type;
    private String coinId;
    private String symbol;
    private String name;
    private String price;
    private String change24h;
    private Long timestamp;
}
