package com.tms.logistica.orderservice.service;

import com.tms.logistica.orderservice.exception.OrdenException;
import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import org.springframework.stereotype.Component;

@Component
public class OrderStateManager {

    public void validarTransicion(EstadoOrden actual, EstadoOrden nuevo) {
        if (actual == EstadoOrden.ANULADA || actual == EstadoOrden.CERRADA) {
            throw new OrdenException("ESTADO_INVALIDO", "La orden ya no permite cambios de estado");
        }
        boolean permitido = switch (actual) {
            case PENDIENTE -> nuevo == EstadoOrden.PROGRAMADA || nuevo == EstadoOrden.ANULADA;
            case PROGRAMADA -> nuevo == EstadoOrden.EN_RUTA || nuevo == EstadoOrden.ANULADA;
            case EN_RUTA -> nuevo == EstadoOrden.ENTREGADA || nuevo == EstadoOrden.ANULADA;
            case ENTREGADA -> nuevo == EstadoOrden.CERRADA;
            case ANULADA, CERRADA -> false;
        };
        if (!permitido) {
            throw new OrdenException("ESTADO_INVALIDO", "Transicion no permitida de " + actual + " a " + nuevo);
        }
    }

    public void validarEditable(EstadoOrden estado) {
        if (estado != EstadoOrden.PENDIENTE && estado != EstadoOrden.PROGRAMADA) {
            throw new OrdenException("ESTADO_INVALIDO", "Solo se puede editar una orden pendiente o programada");
        }
    }

    public void validarAsignable(EstadoOrden estado) {
        if (estado == EstadoOrden.ANULADA || estado == EstadoOrden.CERRADA || estado == EstadoOrden.ENTREGADA) {
            throw new OrdenException("ESTADO_INVALIDO", "La orden no permite asignaciones en estado " + estado);
        }
    }
}
