package com.phuocnt.trading_platform_be.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KlineMessage {

    private String type;
    private String coinId;
    private String symbol;
    private String interval;

    private Long openTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private Boolean closed;

    private Long timestamp;
}
