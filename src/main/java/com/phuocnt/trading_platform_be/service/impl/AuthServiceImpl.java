package com.phuocnt.trading_platform_be.service.impl;

import com.phuocnt.trading_platform_be.dto.request.RegisterRequest;
import com.phuocnt.trading_platform_be.dto.response.AuthResponse;
import com.phuocnt.trading_platform_be.entity.*;
import com.phuocnt.trading_platform_be.enums.RoleCode;
import com.phuocnt.trading_platform_be.exception.ConflictException;
import com.phuocnt.trading_platform_be.repository.*;
import com.phuocnt.trading_platform_be.security.JwtService;
import com.phuocnt.trading_platform_be.service.*;
import com.phuocnt.trading_platform_be.utils.OtpUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TwoFactorOTPService twoFactorOTPService;
    @Autowired
    private EmailService emailService;

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        Role userRole = roleRepository.findByCode(RoleCode.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User newUser = new User();
        newUser.setEmail(req.getEmail());
        newUser.setFullName(req.getFullName());
        newUser.setPassword(passwordEncoder.encode(req.getPassword()));
        newUser.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(newUser);

        String roles = savedUser.getRoles().stream()
                .map(role -> role.getCode().name())
                .collect(Collectors.joining(","));

        String token = jwtService.generateToken(savedUser.getEmail(), roles);

        AuthResponse res = new AuthResponse();
        res.setAccessToken(token);
        res.setStatus(true);
        res.setMessage("Signup successful");
        return res;
    }

    @Override
    public AuthResponse login(String email, String password) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password is incorrect");
        }

        String roles = user.getRoles().stream()
                .map(role -> role.getCode().name())
                .collect(Collectors.joining(","));

        String token = jwtService.generateToken(email, roles);

        if (user.getTwoFactorAuth().isEnabled()) {
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor auth is enabled");
            res.setTwoFactorAuthEnabled(true);

            String otp = OtpUtils.generateOtp();
            TwoFactorOTP oldOtp = twoFactorOTPService.findByUserId(user.getId());
            if (oldOtp != null) {
                twoFactorOTPService.deleteTwoFactorOTP(oldOtp);
            }
            TwoFactorOTP newOtp = twoFactorOTPService.createTwoFactorOTP(user, otp, token);
            emailService.senVerificationOtpEmail(user.getEmail(), otp);

            res.setSession(newOtp.getId());
            return res;
        }

        AuthResponse res = new AuthResponse();
        res.setAccessToken(token);
        res.setStatus(true);
        res.setMessage("Login successful");
        return res;
    }

    @Override
    public AuthResponse verifyOtp(String id, String otp) {
        TwoFactorOTP twoFactorOTP = twoFactorOTPService.findById(id);

        if (twoFactorOTP == null) {
            throw new RuntimeException("Invalid session");
        }

        if (twoFactorOTPService.verifyTwoFactorOTP(twoFactorOTP, otp)) {
            AuthResponse res = new AuthResponse();
            res.setAccessToken(twoFactorOTP.getToken());
            res.setStatus(true);
            res.setMessage("Two factor auth verified successfully");
            res.setTwoFactorAuthEnabled(true);

            twoFactorOTPService.deleteTwoFactorOTP(twoFactorOTP);
            return res;
        }
        throw new RuntimeException("Invalid OTP");
    }
}
