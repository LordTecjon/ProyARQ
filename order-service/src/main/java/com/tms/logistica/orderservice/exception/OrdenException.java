package com.tms.logistica.orderservice.exception;

import lombok.Getter;

@Getter
public class OrdenException extends RuntimeException {

    private final String codigo;

    public OrdenException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }
}
