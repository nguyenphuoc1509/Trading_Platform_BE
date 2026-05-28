package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.ForgotPassword;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.VerificationType;
import com.phuocnt.trading_platform_be.repository.ForgotPasswordRepository;
import com.phuocnt.trading_platform_be.service.ForgotPasswordService;
import com.phuocnt.trading_platform_be.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ForgotPasswordImpl implements ForgotPasswordService {

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Override
    public ForgotPassword createToken(User user, String id, String otp, VerificationType verificationType, String sendTo) {
        forgotPasswordRepository.findByUser(user).ifPresent(forgotPasswordRepository::delete);
        forgotPasswordRepository.flush();

        ForgotPassword token = new ForgotPassword();
        token.setId(id);
        token.setUser(user);
        token.setOtp(otp);
        token.setVerificationType(verificationType);
        token.setSendTo(sendTo);
        token.setExpiredAt(OtpUtils.generateExpiredAt());

        return forgotPasswordRepository.save(token);
    }


    @Override
    public ForgotPassword findById(String id) {
        Optional<ForgotPassword> forgotPassword = forgotPasswordRepository.findById(id);
        return forgotPassword.orElse(null);
    }

    @Override
    public ForgotPassword findByUserId(Long userId) {
        return forgotPasswordRepository.findByUserId(userId);
    }

    @Override
    public void deleteToken(ForgotPassword forgotPassword) {
        forgotPasswordRepository.delete(forgotPassword);
    }
}
