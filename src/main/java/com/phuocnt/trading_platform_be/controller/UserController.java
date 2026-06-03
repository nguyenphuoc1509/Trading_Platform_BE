package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.dto.response.ApiEnvelope;
import com.phuocnt.trading_platform_be.dto.response.UserProfileResponse;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.entity.VerificationCode;
import com.phuocnt.trading_platform_be.enums.VerificationType;
import com.phuocnt.trading_platform_be.mapper.UserMapper;
import com.phuocnt.trading_platform_be.service.EmailService;
import com.phuocnt.trading_platform_be.service.UserService;
import com.phuocnt.trading_platform_be.service.VerificationCodeService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiEnvelope<UserProfileResponse>> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return ResponseEntity.ok(ApiEnvelope.success(UserMapper.toProfileResponse(user)));
    }

    @PostMapping("/verification/{verificationType}/send-otp")
    public ResponseEntity<ApiEnvelope<String>> sendVerificationOtp(
            @PathVariable VerificationType verificationType,
            @AuthenticationPrincipal Jwt jwt) throws MessagingException {

        User user = userService.findByEmail(jwt.getSubject());

        VerificationCode verificationCode = verificationCodeService
                .getVerificationCodeByUserId(user.getId());

        if (verificationCode == null) {
            verificationCode = verificationCodeService.sendVerificationCode(user, verificationType);
        }
        if (verificationType.equals(VerificationType.EMAIL)) {
            emailService.senVerificationOtpEmail(user.getEmail(), verificationCode.getOtp());
        }
        return ResponseEntity.ok(ApiEnvelope.success("Verification OTP sent successfully"));
    }

    @PatchMapping("/enable-two-factor/verify-otp/{otp}")
    public ResponseEntity<ApiEnvelope<UserProfileResponse>> enableTwoFactorAuthentication(
            @PathVariable String otp,
            @AuthenticationPrincipal Jwt jwt) throws Exception {

        User user = userService.findByEmail(jwt.getSubject());

        VerificationCode verificationCode = verificationCodeService
                .getVerificationCodeByUserId(user.getId());

        if (verificationCode.getExpiredAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        String sendTo = verificationCode.getVerificationType()
                .equals(VerificationType.EMAIL)
                ? verificationCode.getEmail()
                : verificationCode.getPhone();

        if (!verificationCode.getOtp().equals(otp)) {
            throw new Exception("Wrong OTP");
        }
        userService.enableTwoFactorAuthentication(
                verificationCode.getVerificationType(), sendTo, user);

        User updatedUser = userService.findByEmail(jwt.getSubject());

        return ResponseEntity.ok(ApiEnvelope.success(UserMapper.toProfileResponse(updatedUser)));
    }
}