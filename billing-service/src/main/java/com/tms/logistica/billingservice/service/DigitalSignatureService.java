package com.tms.logistica.billingservice.service;

import org.springframework.stereotype.Component;

/**
 * Capa de infraestructura: firma digital del XML (Apache Santuario / xmlsec).
 *
 * Implementacion simulada: adjunta un bloque de firma marcador. La firma real
 * con el certificado del emisor queda como trabajo futuro.
 */
@Component
public class DigitalSignatureService {

    public String firmar(String xml) {
        return xml + "\n<!-- Signature: SIMULADA (Apache Santuario pendiente) -->";
    }
}
