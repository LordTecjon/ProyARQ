package com.tms.logistica.authservice.application.exception;
public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) { super(message); }
}
