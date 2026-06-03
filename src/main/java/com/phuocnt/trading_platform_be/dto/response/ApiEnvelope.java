package com.phuocnt.trading_platform_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiEnvelope<T> {
    private String code;
    private String msg;
    private T data;
    private Long timestamp;

    public static <T> ApiEnvelope<T> success(T data) {
        return ApiEnvelope.<T>builder()
                .code("0")
                .msg("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiEnvelope<T> error(T data) {
        return ApiEnvelope.<T>builder()
                .code("-1")
                .msg("error")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
