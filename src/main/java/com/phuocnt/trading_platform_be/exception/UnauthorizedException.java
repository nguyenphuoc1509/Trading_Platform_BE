package com.phuocnt.trading_platform_be.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException{
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
