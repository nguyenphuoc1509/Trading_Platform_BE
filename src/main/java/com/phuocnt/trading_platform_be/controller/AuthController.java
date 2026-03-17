package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.dto.response.AuthResponse;
import com.phuocnt.trading_platform_be.entity.Role;
import com.phuocnt.trading_platform_be.entity.TwoFactorOTP;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.RoleCode;
import com.phuocnt.trading_platform_be.exception.ConflictException;
import com.phuocnt.trading_platform_be.repository.RoleRepository;
import com.phuocnt.trading_platform_be.repository.UserRepository;
import com.phuocnt.trading_platform_be.security.JwtService;
import com.phuocnt.trading_platform_be.service.EmailService;
import com.phuocnt.trading_platform_be.service.TwoFactorOTPService;
import com.phuocnt.trading_platform_be.utils.OtpUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

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

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        Role userRole = roleRepository.findByCode(RoleCode.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFullName(user.getFullName());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(newUser);

        // Generate token luôn sau khi signup
        String roles = savedUser.getRoles().stream()
                .map(role -> role.getCode().name())
                .collect(Collectors.joining(","));

        String token = jwtService.generateToken(savedUser.getEmail(), roles);

        AuthResponse res = new AuthResponse();
        res.setAccessToken(token);
        res.setStatus(true);
        res.setMessage("Signup successful");

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> loginRequest) throws MessagingException {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password is incorrect");
        }

        String roles = user.getRoles().stream()
                .map(role -> role.getCode().name())
                .collect(Collectors.joining(","));

        String token = jwtService.generateToken(email, roles);

        if (user.getTwoFactorAuth().isEnabled()){
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor auth is enabled");
            res.setTwoFactorAuthEnabled(true);
            String otp = OtpUtils.generateOtp();

            TwoFactorOTP oldTwoFactorOtp = twoFactorOTPService.findByUserId(user.getId());
            if (oldTwoFactorOtp != null){
                twoFactorOTPService.deleteTwoFactorOTP(oldTwoFactorOtp);
            }

            TwoFactorOTP newTwoFactorOTP = twoFactorOTPService.createTwoFactorOTP(user, otp, token);

            emailService.senVerificationOtpEmail(user.getEmail(), otp);


            res.setSession(newTwoFactorOTP.getId());
            return new ResponseEntity<>(res, HttpStatus.ACCEPTED);

        }

        AuthResponse res = new AuthResponse();
        res.setAccessToken(token);
        res.setStatus(true);
        res.setMessage("Login successful");

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/two-factor/otp/{otp}")
    public ResponseEntity<AuthResponse> verifyOtp(@PathVariable String otp,
                                                  @RequestParam String id){
        TwoFactorOTP twoFactorOTP = twoFactorOTPService.findById(id);
        if (twoFactorOTPService.verifyTwoFactorOTP(twoFactorOTP, otp)) {
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor auth is verified");
            res.setTwoFactorAuthEnabled(true);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        return null;
    }
}
