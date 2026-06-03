package com.phuocnt.trading_platform_be.dto.response;

import com.phuocnt.trading_platform_be.enums.TransactionStatus;
import com.phuocnt.trading_platform_be.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WalletTransactionResponse {
    private Long txId;
    private String amount;
    private TransactionType type;
    private TransactionStatus status;
    private String purpose;
    private Long createdAt ;
}
