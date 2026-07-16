package com.tms.logistica.costservice.service;

import com.tms.logistica.costservice.exception.CostoException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CostIntegrationClient {

    public void validarOrden(Long ordenId) {
        if (ordenId == null || ordenId <= 0) {
            throw new CostoException("ORDEN_INVALIDA", "La orden asociada no es valida");
        }
    }

    public BigDecimal obtenerIngresoFacturado(Long ordenId) {
        return null;
    }
}
