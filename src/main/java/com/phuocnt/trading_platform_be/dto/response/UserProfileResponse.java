package com.phuocnt.trading_platform_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private Long uid;
    private String fullName;
    private String email;
    private boolean twoFactorEnabled;
    private String twoFactorType;
}
