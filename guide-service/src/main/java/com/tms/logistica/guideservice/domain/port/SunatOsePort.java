package com.tms.logistica.guideservice.domain.port;

import com.tms.logistica.guideservice.domain.valueobject.ResultadoEnvioSunat;
import com.tms.logistica.guideservice.model.entity.GuiaRemision;

/**
 * SunatOsePort — Puerto de salida del dominio hacia el OSE/SUNAT
 *
 * Define el contrato que el dominio del guide-service necesita para enviar
 * una Guía de Remisión Electrónica (GRE) al Operador de Servicios Electrónicos
 * (OSE) que la procesa ante SUNAT.
 *
 * PATRÓN: Anti-Corruption Layer (puerto de salida / output port)
 *
 * El dominio (GuiaService) solo conoce esta interfaz. No sabe nada de:
 *   - Qué OSE se usa (APIsPerú, Nubefact, EFACT, etc.)
 *   - Cómo se autentica (Bearer token, certificado, etc.)
 *   - El formato del request (JSON, SOAP, XML)
 *   - Cómo se parsea la respuesta
 *
 * La implementación concreta (SunatOseAclAdapter en el paquete acl/sunat/)
 * encapsula todos esos detalles externos y traduce los resultados al
 * value object ResultadoEnvioSunat del dominio.
 *
 * Si el OSE no responde o hay error de red, el adaptador lanza
 * RuntimeException para que GuiaService encole la guía para reintento.
 */
public interface SunatOsePort {

    /**
     * Envía la guía al OSE y retorna el resultado del CDR de SUNAT.
     *
     * @param guia entidad con todos los datos de la GRE a enviar
     * @return ResultadoEnvioSunat con código CDR, descripción y flag de aceptación
     * @throws RuntimeException si hay error de red o el OSE no responde
     */
    ResultadoEnvioSunat enviar(GuiaRemision guia);
}
