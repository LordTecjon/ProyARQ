package com.tms.logistica.billingservice.service;

import com.tms.logistica.billingservice.model.entity.Comprobante;
import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Capa de infraestructura: envio del comprobante firmado a SUNAT/OSE y
 * procesamiento de la CDR de respuesta (ADR-8.2.3).
 *
 * Implementacion simulada: acepta el comprobante y devuelve una CDR ficticia.
 * El envio real (WebClient sobre TLS 1.2+), la cola de reenvio (ColaEnvio) y
 * el worker @Scheduled con back-off quedan como trabajo futuro.
 */
@Component
@Slf4j
public class SunatGatewayClient {

    /** Resultado del envio: estado resultante y CDR asociada. */
    public record ResultadoEnvio(EstadoComprobante estado, String cdr) {}

    public ResultadoEnvio enviar(Comprobante comprobante) {
        log.info("Enviando comprobante {}-{} a SUNAT (simulado)",
                comprobante.getSerie(), comprobante.getCorrelativo());
        String cdr = "CDR-ACEPTADO-" + comprobante.getSerie() + comprobante.getCorrelativo();
        return new ResultadoEnvio(EstadoComprobante.ACEPTADO, cdr);
    }
}
