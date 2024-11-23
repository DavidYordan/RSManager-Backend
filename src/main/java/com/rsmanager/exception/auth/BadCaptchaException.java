package com.rsmanager.exception.auth;

public class BadCaptchaException extends RuntimeException {
    public BadCaptchaException(String message) {
        super(message);
    }
}
