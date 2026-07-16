package com.tms.logistica.billingservice.service;

import com.tms.logistica.billingservice.model.entity.Comprobante;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Capa de infraestructura: notificacion del comprobante (PDF+XML) al cliente
 * por correo (JavaMailSender).
 *
 * Implementacion simulada: registra el envio en el log. El envio SMTP real
 * queda como trabajo futuro.
 */
@Component
@Slf4j
public class NotificacionClienteService {

    public void notificar(Comprobante comprobante) {
        log.info("Notificando comprobante {}-{} al cliente {} (simulado)",
                comprobante.getSerie(), comprobante.getCorrelativo(), comprobante.getClienteId());
    }
}
