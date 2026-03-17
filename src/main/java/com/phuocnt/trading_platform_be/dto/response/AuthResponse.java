package com.phuocnt.trading_platform_be.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private boolean status;
    private String message;
    private boolean isTwoFactorAuthEnabled;
    private String session;
}
