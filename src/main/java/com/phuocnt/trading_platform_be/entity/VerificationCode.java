package com.phuocnt.trading_platform_be.entity;

import com.phuocnt.trading_platform_be.enums.VerificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String otp;

    @OneToOne
    private User user;

    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;
}
