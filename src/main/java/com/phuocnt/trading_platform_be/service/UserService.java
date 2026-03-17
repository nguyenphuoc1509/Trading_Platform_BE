package com.phuocnt.trading_platform_be.service;

import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserService {
    Optional<User> finUserByToken(String token);
    Optional<User>  findByEmail(String email);
    Optional<User> findById(Long userId);
    Optional<User> enableTwoFactorAuthentication(VerificationType verificationType, String sendTo, User user);
    Optional<User> updatePassword(User user, String newPassword);
}
