package com.phuocnt.trading_platform_be.entity;

import com.phuocnt.trading_platform_be.enums.RoleCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false, unique = true, length = 50)
    private RoleCode code;

    @Column( nullable = false, unique = true, length = 50)
    private String name;
}
