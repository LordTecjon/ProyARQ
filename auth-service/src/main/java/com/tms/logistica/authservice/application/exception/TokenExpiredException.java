package com.tms.logistica.authservice.application.exception;
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) { super(message); }
}
