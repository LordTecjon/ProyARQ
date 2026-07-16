package com.tms.logistica.guideservice.domain.valueobject;

/**
 * ResultadoEnvioSunat — Value Object del Dominio
 *
 * Representa el resultado de enviar una Guía de Remisión Electrónica al OSE
 * (Operador de Servicios Electrónicos) que la retransmite a SUNAT.
 *
 * Este VO pertenece al dominio del guide-service y NO contiene conceptos
 * específicos de ningún proveedor externo (APIsPerú, OSE, etc.).
 * El Anti-Corruption Layer traduce las respuestas externas a este tipo.
 *
 * Semántica del campo codigoCdr:
 *   "0"    → SUNAT aceptó la GRE
 *   "2XXX" → SUNAT rechazó la GRE con el código de error indicado
 *   "ERROR"→ el OSE no pudo procesar la solicitud (error interno del proveedor)
 */
public record ResultadoEnvioSunat(

        /** Código CDR devuelto por SUNAT. "0" = aceptada. */
        String codigoCdr,

        /** Descripción legible del resultado para mostrar al usuario. */
        String descripcionCdr,

        /**
         * true si codigoCdr == "0", es decir, si SUNAT aceptó la guía.
         * Simplifica la lógica de negocio en GuiaService.
         */
        boolean aceptada,

        /**
         * Respuesta JSON cruda devuelta por el OSE.
         * Se persiste en la entidad GuiaRemision para auditoría y trazabilidad.
         * No se expone en los DTOs de respuesta al cliente.
         */
        String rawResponse
) {}
