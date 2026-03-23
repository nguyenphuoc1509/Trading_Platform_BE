package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.dto.request.ForgotPasswordTokenRequest;
import com.phuocnt.trading_platform_be.dto.request.ResetPasswordRequest;
import com.phuocnt.trading_platform_be.dto.response.ApiResponse;
import com.phuocnt.trading_platform_be.dto.response.AuthResponse;
import com.phuocnt.trading_platform_be.entity.ForgotPassword;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.entity.VerificationCode;
import com.phuocnt.trading_platform_be.enums.VerificationType;
import com.phuocnt.trading_platform_be.service.AuthService;
import com.phuocnt.trading_platform_be.service.EmailService;
import com.phuocnt.trading_platform_be.service.ForgotPasswordService;
import com.phuocnt.trading_platform_be.service.UserService;
import com.phuocnt.trading_platform_be.utils.OtpUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;
    @Autowired
    private EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) {
        return new ResponseEntity<>(authService.register(user), HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> loginRequest) throws MessagingException {
        return ResponseEntity.ok(authService.login(loginRequest.get("email"), loginRequest.get("password")));
    }

    @PostMapping("/two-factor/otp/{otp}")
    public ResponseEntity<AuthResponse> verifyOtp(@PathVariable String otp, @RequestParam String id) {
        return ResponseEntity.ok(authService.verifyOtp(id, otp));
    }

    @PostMapping("/reset-password/send-otp")
    public ResponseEntity<AuthResponse> sendPasswordOtp(@RequestBody ForgotPasswordTokenRequest req)
            throws MessagingException {
        User user = userService.findByEmail(req.getSendTo());
        String otp = OtpUtils.generateOtp();
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();

        ForgotPassword forgotPassword = forgotPasswordService.findByUserId(user.getId());

        if (forgotPassword == null) {
            forgotPassword = forgotPasswordService.createToken(user, id, otp, req.getVerificationType(),req.getSendTo());
        }

        if (req.getVerificationType().equals(VerificationType.EMAIL)) {
            emailService.senVerificationOtpEmail(user.getEmail(), forgotPassword.getOtp());
        }
        AuthResponse res = new AuthResponse();
        res.setSession(forgotPassword.getId());
        res.setMessage("Otp reset password sent successfully");

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/reset-password/verify-otp")
    public ResponseEntity<ApiResponse> verifyPasswordOtp(@RequestParam String id,
                                                    @RequestBody ResetPasswordRequest req) {
        ForgotPassword forgotPassword = forgotPasswordService.findById(id);

        boolean isVerified = forgotPassword.getOtp().equals(req.getOtp());

        if(isVerified) {
            userService.updatePassword(forgotPassword.getUser(), req.getPassword());
            ApiResponse res = new ApiResponse();
            res.setMessage("Password update successfully");
            res.setStatus(true);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        }
        throw new RuntimeException("Wrong Otp");
    }
}
