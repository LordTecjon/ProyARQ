package com.tms.logistica.guideservice.domain.valueobject;

/**
 * InfoDni — Value Object del Dominio
 *
 * Representa los datos de una persona natural registrada en RENIEC,
 * expresados en términos del dominio del negocio (no en términos de APIsPerú).
 *
 * El Anti-Corruption Layer (ApisPeruIdentidadAdapter + IdentidadResponseTranslator)
 * traduce la respuesta JSON de dniruc.apisperu.com a este VO antes de entregarlo
 * al dominio.
 *
 * Se usa principalmente para validar y obtener el nombre oficial del conductor
 * de una GRE al momento de su creación.
 */
public record InfoDni(

        /** Número de DNI de 8 dígitos. */
        String dni,

        /** Nombres de pila (sin apellidos). */
        String nombres,

        /** Primer apellido. */
        String apellidoPaterno,

        /** Segundo apellido. */
        String apellidoMaterno,

        /**
         * Nombre completo en el orden estándar peruano:
         * "{apellidoPaterno} {apellidoMaterno} {nombres}".
         * Listo para usar en documentos oficiales como la GRE.
         */
        String nombreCompleto
) {}
