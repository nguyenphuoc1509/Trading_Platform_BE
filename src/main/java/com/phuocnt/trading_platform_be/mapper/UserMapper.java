package com.phuocnt.trading_platform_be.mapper;

import com.phuocnt.trading_platform_be.dto.response.UserProfileResponse;
import com.phuocnt.trading_platform_be.entity.TwoFactorAuth;
import com.phuocnt.trading_platform_be.entity.User;

public class UserMapper {

    public static UserProfileResponse toProfileResponse(User user) {
        TwoFactorAuth tfa = user.getTwoFactorAuth();
        boolean tfaEnaabled = tfa != null && tfa.isEnabled();
        String tfaType = (tfaEnaabled && tfa.getVerificationType() != null)
                ? tfa.getVerificationType().name()
                : null;
        return UserProfileResponse.builder()
                .uid(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .twoFactorEnabled(tfaEnaabled)
                .twoFactorType(tfaType)
                .build();
    }
}
