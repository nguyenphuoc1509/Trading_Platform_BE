package com.phuocnt.trading_platform_be.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class OtpUtils {

    public static String generateOtp() {
        int otpLength = 6;
        SecureRandom random = new SecureRandom(); // fix luôn lỗi số 4
        StringBuilder otp = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public static LocalDateTime generateExpiredAt() {
        return LocalDateTime.now().plusMinutes(5); // OTP hết hạn sau 5 phút
    }
}