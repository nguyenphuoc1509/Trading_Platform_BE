package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.entity.TwoFactorAuth;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.VerificationType;
import com.phuocnt.trading_platform_be.repository.UserRepository;
import com.phuocnt.trading_platform_be.security.JwtService;
import com.phuocnt.trading_platform_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
        user.setPassword(passwordEncoder.encode(newPassword));

        return Optional.of(userRepository.save(user));
    }
}

