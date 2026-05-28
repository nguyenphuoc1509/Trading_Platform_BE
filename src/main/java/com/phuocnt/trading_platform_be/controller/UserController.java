package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.entity.VerificationCode;
import com.phuocnt.trading_platform_be.enums.VerificationType;
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
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findByEmail(jwt.getSubject());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/verification/{verificationType}/send-otp")
    public ResponseEntity<String> sendVerificationOtp(
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
        return new ResponseEntity<>("Verification otp sent successful", HttpStatus.OK);
    }

    @PatchMapping("/enable-two-factor/verify-otp/{otp}")
    public ResponseEntity<User> enableTwoFactorAuthentication(
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

        boolean isVerified = verificationCode.getOtp().equals(otp);
        if (isVerified) {
            userService.enableTwoFactorAuthentication(
                    verificationCode.getVerificationType(), sendTo, user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        throw new Exception("Wrong Otp");
    }
}