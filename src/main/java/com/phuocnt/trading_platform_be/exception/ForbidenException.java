package com.phuocnt.trading_platform_be.exception;

import org.springframework.http.HttpStatus;

public class ForbidenException extends ApiException{
    public ForbidenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
