package com.phuocnt.trading_platform_be.entity;

import com.phuocnt.trading_platform_be.enums.VerificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuth {
    private boolean isEnabled = false;
    private VerificationType verificationType;
}
