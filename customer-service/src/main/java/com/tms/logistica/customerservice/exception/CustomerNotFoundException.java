package com.tms.logistica.customerservice.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(UUID id) {
        super("Cliente no encontrado con ID: " + id);
    }
    public CustomerNotFoundException(String ruc) {
        super("Cliente no encontrado con RUC: " + ruc);
    }
}
