package com.phuocnt.trading_platform_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KlineResponse {
    private Long openTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private Long closeTime;
}
