package com.tms.logistica.billingservice.service;

import com.tms.logistica.billingservice.exception.BillingException;
import com.tms.logistica.billingservice.model.entity.Comprobante;
import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import com.tms.logistica.billingservice.model.enums.TipoComprobante;
import org.springframework.stereotype.Component;

/**
 * Reglas de transicion de estado del comprobante. Concentra las validaciones
 * de que operaciones se permiten segun el estado actual.
 */
@Component
public class ComprobanteEstadoManager {

    public void validarAnulable(EstadoComprobante estado) {
        if (estado == EstadoComprobante.ANULADO) {
            throw new BillingException("ESTADO_INVALIDO", "El comprobante ya esta anulado");
        }
    }

    public void validarPagable(EstadoComprobante estado) {
        if (estado != EstadoComprobante.ACEPTADO && estado != EstadoComprobante.ACEPTADO_OBS) {
            throw new BillingException("ESTADO_INVALIDO",
                    "Solo se pueden registrar pagos sobre comprobantes aceptados por SUNAT");
        }
    }

    public void validarReenviable(EstadoComprobante estado) {
        if (estado != EstadoComprobante.EN_COLA && estado != EstadoComprobante.RECHAZADO) {
            throw new BillingException("ESTADO_INVALIDO",
                    "Solo se reenvian comprobantes en cola o rechazados");
        }
    }

    public void validarNotaCreditoAplicable(Comprobante origen) {
        if (origen.getTipo() == TipoComprobante.NOTA_CREDITO) {
            throw new BillingException("ESTADO_INVALIDO",
                    "No se puede emitir una nota de credito sobre otra nota de credito");
        }
        if (origen.getEstado() != EstadoComprobante.ACEPTADO
                && origen.getEstado() != EstadoComprobante.ACEPTADO_OBS) {
            throw new BillingException("ESTADO_INVALIDO",
                    "Solo se emiten notas de credito sobre comprobantes aceptados por SUNAT");
        }
    }
}
