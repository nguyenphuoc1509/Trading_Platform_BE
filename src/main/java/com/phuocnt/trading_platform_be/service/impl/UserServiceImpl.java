package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.TwoFactorAuth;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.VerificationType;
import com.phuocnt.trading_platform_be.repository.UserRepository;
import com.phuocnt.trading_platform_be.security.JwtService;
import com.phuocnt.trading_platform_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;


    @Override
    public Optional<User> finUserByToken(String token) {
        String email = jwtService.getEmailFromToken(token);
        Optional<User> user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    @Override
    public Optional<User> enableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user) {
        TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
        twoFactorAuth.setEnabled(true);
        twoFactorAuth.setVerificationType(verificationType);

        user.setTwoFactorAuth(twoFactorAuth);

        return Optional.of(userRepository.save(user));
    }

    @Override
    public Optional<User> updatePassword(User user, String newPassword) {
        user.setPassword(newPassword);

        return Optional.of(userRepository.save(user));
    }
}

