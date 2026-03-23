package com.phuocnt.trading_platform_be.dto.request;

import com.phuocnt.trading_platform_be.enums.VerificationType;
import lombok.Data;

@Data
public class ForgotPasswordTokenRequest {
    private String sendTo;
    private VerificationType verificationType;
}
