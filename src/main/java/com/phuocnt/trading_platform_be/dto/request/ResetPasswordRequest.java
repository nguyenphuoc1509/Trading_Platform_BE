package com.phuocnt.trading_platform_be.dto.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String otp;
    private String password;
}
