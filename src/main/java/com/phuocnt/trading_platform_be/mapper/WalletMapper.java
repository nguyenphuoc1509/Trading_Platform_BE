package com.phuocnt.trading_platform_be.mapper;

import com.phuocnt.trading_platform_be.dto.response.WalletResponse;
import com.phuocnt.trading_platform_be.dto.response.WalletTransactionResponse;
import com.phuocnt.trading_platform_be.entity.Wallet;
import com.phuocnt.trading_platform_be.entity.WalletTransaction;

import java.util.List;
import java.util.stream.Collectors;

import static com.phuocnt.trading_platform_be.mapper.MapperUtils.*;

public class WalletMapper {

    public static WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .balance(fmt4(wallet.getBalance()))
                .currency(wallet.getCurrency())
                .build();
    }

    public static WalletTransactionResponse toTxResponse(WalletTransaction tx) {
        return  WalletTransactionResponse.builder()
                .txId(tx.getId())
                .amount(fmt4(tx.getAmount()))
                .type(tx.getType())
                .status(tx.getStatus())
                .purpose(tx.getPurpose())
                .createdAt(toMillis(tx.getCreatedAt()))
                .build();
    }

    public static List<WalletTransactionResponse> toTxResponseList(List<WalletTransaction> txs) {
        return txs.stream().map(WalletMapper::toTxResponse).collect(Collectors.toList());
    }
}
