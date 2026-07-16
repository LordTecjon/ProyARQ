package com.tms.logistica.billingservice.service;

import com.tms.logistica.billingservice.model.entity.Comprobante;
import com.tms.logistica.billingservice.model.entity.LineaComprobante;
import com.tms.logistica.billingservice.repository.ParametroTributarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Capa de dominio: unico punto de calculo tributario (subtotal, IGV y total).
 * La tasa de IGV vive en ParametroTributario; ante un cambio por decreto solo
 * se inserta un nuevo parametro vigente sin tocar esta logica (ADR-4.2.4).
 */
@Component
@RequiredArgsConstructor
public class CalculoTributarioService {

    private static final String CODIGO_IGV = "IGV_RATE";
    private static final BigDecimal IGV_POR_DEFECTO = new BigDecimal("0.18");

    private final ParametroTributarioRepository parametroRepository;

    public BigDecimal tasaIgvVigente() {
        return parametroRepository
                .findFirstByCodigoAndVigenteHastaIsNullOrderByVigenteDesdeDesc(CODIGO_IGV)
                .map(p -> p.getValor())
                .orElse(IGV_POR_DEFECTO);
    }

    /** Calcula el subtotal de cada linea y consolida subtotal, IGV, total y saldo del comprobante. */
    public void aplicarCalculos(Comprobante comprobante) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineaComprobante linea : comprobante.getLineas()) {
            BigDecimal subtotalLinea = linea.getPrecioUnitario()
                    .multiply(linea.getCantidad())
                    .setScale(2, RoundingMode.HALF_UP);
            linea.setSubtotalLinea(subtotalLinea);
            subtotal = subtotal.add(subtotalLinea);
        }
        BigDecimal igv = subtotal.multiply(tasaIgvVigente()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        comprobante.setSubtotal(subtotal);
        comprobante.setIgv(igv);
        comprobante.setTotal(total);
        comprobante.setSaldoPendiente(total.subtract(comprobante.getMontoPagado()));
    }
}
