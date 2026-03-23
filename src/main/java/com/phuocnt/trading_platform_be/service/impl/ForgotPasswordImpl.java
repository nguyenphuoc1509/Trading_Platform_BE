package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.ForgotPassword;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.VerificationType;
import com.phuocnt.trading_platform_be.repository.ForgotPasswordRepository;
import com.phuocnt.trading_platform_be.service.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ForgotPasswordImpl implements ForgotPasswordService {

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Override
    public ForgotPassword createToken(User user, String id, String otp,
                                      VerificationType verificationType, String sendTo) {
        ForgotPassword token = new ForgotPassword();
        token.setUser(user);
        token.setOtp(otp);
        token.setVerificationType(verificationType);
        token.setSendTo(sendTo);
        token.setId(id);

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
