package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.TwoFactorOTP;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.repository.TwoFactorOTPRepository;
import com.phuocnt.trading_platform_be.service.TwoFactorOTPService;
import com.phuocnt.trading_platform_be.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TwoFactorOTPServiceImpl implements TwoFactorOTPService {

    @Autowired
    private TwoFactorOTPRepository twoFactorOTPRepository;

    @Override
    public TwoFactorOTP createTwoFactorOTP(User user, String otpCode, String token) {
        UUID uuid = UUID.randomUUID();

        String id = uuid.toString();

        TwoFactorOTP twoFactorOTP = new TwoFactorOTP();
        twoFactorOTP.setId(id);
        twoFactorOTP.setOtp(otpCode);
        twoFactorOTP.setUser(user);
        twoFactorOTP.setToken(token);
        twoFactorOTP.setExpiredAt(OtpUtils.generateExpiredAt());
        return twoFactorOTPRepository.save(twoFactorOTP);
    }

    @Override
    public TwoFactorOTP findByUserId(Long userId) {
        return twoFactorOTPRepository.findByUserId(userId);
    }

    @Override
    public TwoFactorOTP findById(String id) {
        Optional<TwoFactorOTP> twoFactorOTP = twoFactorOTPRepository.findById(id);
        return twoFactorOTP.orElse(null);
    }

    @Override
    public boolean verifyTwoFactorOTP(TwoFactorOTP twoFactorOTP, String otpCode) {
        if (twoFactorOTP.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }
        return twoFactorOTP.getOtp().equals(otpCode);
    }

    @Override
    public void deleteTwoFactorOTP(TwoFactorOTP twoFactorOTP) {
        twoFactorOTPRepository.delete(twoFactorOTP);
    }
}
