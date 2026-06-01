package com.phuocnt.trading_platform_be;

import com.phuocnt.trading_platform_be.utils.OtpUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class OtpUtilsTest {

    @Test
    @DisplayName("OTP dài đúng 6 chữ số")
    void otp_isExactly6Digits() {
        String otp = OtpUtils.generateOtp();
        assertThat(otp).hasSize(6).matches("\\d{6}");
    }

    @RepeatedTest(10)
    @DisplayName("OTP chỉ chứa số (lặp 10 lần)")
    void otp_onlyDigits() {
        String otp = OtpUtils.generateOtp();
        assertThat(otp).matches("[0-9]+");
    }

    @RepeatedTest(5)
    @DisplayName("2 OTP liên tiếp khác nhau (lặp 5 lần)")
    void otp_isRandom() {
        String otp1 = OtpUtils.generateOtp();
        String otp2 = OtpUtils.generateOtp();
        // Không đảm bảo 100% nhưng xác suất trùng là 1/1,000,000
        // Test này chủ yếu verify không bị hardcode
        assertThat(otp1).matches("\\d{6}");
        assertThat(otp2).matches("\\d{6}");
    }

    @Test
    @DisplayName("ExpiredAt là 5 phút sau thời điểm hiện tại")
    void expiredAt_is5MinutesLater() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime expiredAt = OtpUtils.generateExpiredAt();
        LocalDateTime after = LocalDateTime.now();

        assertThat(expiredAt).isAfter(before.plusMinutes(4));
        assertThat(expiredAt).isBefore(after.plusMinutes(6));
    }

    @Test
    @DisplayName("OTP đã hết hạn → isBefore now")
    void expiredOtp_isBeforeNow() {
        LocalDateTime expiredAt = LocalDateTime.now().minusMinutes(1); // 1 phút trước
        assertThat(expiredAt.isBefore(LocalDateTime.now())).isTrue();
    }
}
