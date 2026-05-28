package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.dto.request.RegisterRequest;
import com.phuocnt.trading_platform_be.dto.response.AuthResponse;
import com.phuocnt.trading_platform_be.entity.User;
import jakarta.mail.MessagingException;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(String email, String password) throws MessagingException;
    AuthResponse verifyOtp(String id, String otp);
}
