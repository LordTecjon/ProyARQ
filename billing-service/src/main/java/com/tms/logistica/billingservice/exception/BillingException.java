package com.tms.logistica.billingservice.exception;

import lombok.Getter;

@Getter
public class BillingException extends RuntimeException {

    private final String codigo;

    public BillingException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }
}
