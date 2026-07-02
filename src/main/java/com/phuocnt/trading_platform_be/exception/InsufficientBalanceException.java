package com.phuocnt.trading_platform_be.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends ApiException{
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
