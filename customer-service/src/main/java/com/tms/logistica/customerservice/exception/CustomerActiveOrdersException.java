package com.tms.logistica.customerservice.exception;

public class CustomerActiveOrdersException extends RuntimeException {
    public CustomerActiveOrdersException() {
        super("No se puede inactivar el cliente: tiene órdenes de transporte activas (pendiente, programada o en ruta).");
    }
}
