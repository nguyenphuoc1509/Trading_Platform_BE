package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.entity.ForgotPassword;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.VerificationType;

import java.util.Optional;

public interface ForgotPasswordService {
    ForgotPassword createToken(User user, String id, String otp, VerificationType verificationType, String sendTo);
    ForgotPassword findById(String id);
    ForgotPassword findByUserId(Long userId);
    void deleteToken(ForgotPassword forgotPassword);
}
