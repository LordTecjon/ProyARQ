package com.tms.logistica.guideservice.domain.port;

import com.tms.logistica.guideservice.domain.valueobject.InfoDni;
import com.tms.logistica.guideservice.domain.valueobject.InfoRuc;

import java.util.Optional;

/**
 * IdentidadValidadorPort — Puerto de salida del dominio hacia registros de identidad
 *
 * Define el contrato que el dominio necesita para validar identidades
 * fiscales (RUC vía padrón SUNAT) y personales (DNI vía RENIEC).
 *
 * PATRÓN: Anti-Corruption Layer (puerto de salida / output port)
 *
 * El dominio (GuiaService, ValidacionController) solo conoce esta interfaz.
 * No sabe nada de:
 *   - Qué proveedor se usa (APIsPerú, Sunat directa, etc.)
 *   - El formato de la URL ni los query params del token
 *   - Cómo se parsea el JSON de respuesta
 *   - Las estructuras internas del proveedor externo
 *
 * La implementación concreta (ApisPeruIdentidadAdapter en acl/identidad/)
 * encapsula todos esos detalles y traduce a los value objects del dominio.
 *
 * Ambos métodos retornan Optional para expresar semánticamente que
 * la consulta puede no encontrar resultados (RUC inválido, DNI inexistente).
 * Si hay error de red, el adaptador lanza RuntimeException que GuiaService
 * captura como best-effort.
 */
public interface IdentidadValidadorPort {

    /**
     * Consulta información de un RUC en el padrón de SUNAT.
     *
     * @param ruc número de RUC de 11 dígitos
     * @return Optional con los datos del contribuyente, vacío si no existe
     * @throws RuntimeException si hay error de comunicación con el proveedor
     */
    Optional<InfoRuc> validarRuc(String ruc);

    /**
     * Consulta información de un DNI en el padrón de RENIEC.
     *
     * @param dni número de DNI de 8 dígitos
     * @return Optional con los datos del ciudadano, vacío si no existe
     * @throws RuntimeException si hay error de comunicación con el proveedor
     */
    Optional<InfoDni> validarDni(String dni);
}
