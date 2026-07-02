package com.phuocnt.trading_platform_be.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException{
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
