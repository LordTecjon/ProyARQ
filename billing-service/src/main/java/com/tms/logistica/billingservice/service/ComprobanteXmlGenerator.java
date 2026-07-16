package com.tms.logistica.billingservice.service;

import com.tms.logistica.billingservice.model.entity.Comprobante;
import org.springframework.stereotype.Component;

/**
 * Capa de infraestructura: genera el XML UBL 2.1 del comprobante.
 * Punto unico de cambio ante actualizaciones del esquema SUNAT (ADR-8.2.1).
 *
 * Implementacion simulada: produce un XML minimo representativo. La generacion
 * real con JAXB queda como trabajo futuro (requiere el esquema UBL completo).
 */
@Component
public class ComprobanteXmlGenerator {

    public String generar(Comprobante comprobante) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Invoice>
                  <ID>%s-%s</ID>
                  <IssueDate>%s</IssueDate>
                  <DocumentCurrencyCode>%s</DocumentCurrencyCode>
                  <TaxTotal>%s</TaxTotal>
                  <PayableAmount>%s</PayableAmount>
                </Invoice>
                """.formatted(
                comprobante.getSerie(),
                comprobante.getCorrelativo(),
                comprobante.getFechaEmision(),
                comprobante.getMoneda(),
                comprobante.getIgv(),
                comprobante.getTotal());
    }
}
