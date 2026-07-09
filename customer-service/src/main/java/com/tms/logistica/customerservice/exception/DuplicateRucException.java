package com.tms.logistica.customerservice.exception;

public class DuplicateRucException extends RuntimeException {
    public DuplicateRucException(String ruc) {
        super("Ya existe un cliente registrado con el RUC: " + ruc);
    }
}
