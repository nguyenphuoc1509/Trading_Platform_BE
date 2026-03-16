package com.phuocnt.trading_platform_be.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.phuocnt.trading_platform_be.enums.RoleCode;
import com.phuocnt.trading_platform_be.enums.VerificationType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;


    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Embedded
    private TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
}
