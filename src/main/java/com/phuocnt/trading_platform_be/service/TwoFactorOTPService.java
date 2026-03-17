package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.entity.TwoFactorOTP;
import com.phuocnt.trading_platform_be.entity.User;

public interface TwoFactorOTPService {

    TwoFactorOTP createTwoFactorOTP(User user, String otpCode, String token);
    TwoFactorOTP findByUserId(Long userId);
    TwoFactorOTP findById(String id);
    boolean verifyTwoFactorOTP(TwoFactorOTP twoFactorOTP, String otpCode);

    void deleteTwoFactorOTP(TwoFactorOTP twoFactorOTP);
}
