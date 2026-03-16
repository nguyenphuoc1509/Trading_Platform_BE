package com.phuocnt.trading_platform_be.controller;

import com.phuocnt.trading_platform_be.entity.Role;
import com.phuocnt.trading_platform_be.entity.User;
import com.phuocnt.trading_platform_be.enums.RoleCode;
import com.phuocnt.trading_platform_be.repository.RoleRepository;
import com.phuocnt.trading_platform_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @RequestMapping("/signup")
    public ResponseEntity<User> register(@RequestBody User user) {
        Role userRole = roleRepository.findByCode(RoleCode.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFullName(user.getFullName());
        newUser.setPassword(user.getPassword());
        newUser.setRoles(Set.of(userRole));

        User response = userRepository.save(newUser);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }
}
