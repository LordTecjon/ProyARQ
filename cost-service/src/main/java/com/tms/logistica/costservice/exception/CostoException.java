package com.tms.logistica.costservice.exception;

import lombok.Getter;

@Getter
public class CostoException extends RuntimeException {

    private final String codigo;

    public CostoException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }
}
