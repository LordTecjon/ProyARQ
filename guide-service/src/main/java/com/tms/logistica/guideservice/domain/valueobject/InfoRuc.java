package com.tms.logistica.guideservice.domain.valueobject;

/**
 * InfoRuc — Value Object del Dominio
 *
 * Representa los datos de un contribuyente registrado en el padrón de SUNAT,
 * expresados en términos del dominio del negocio (no en términos de APIsPerú).
 *
 * El Anti-Corruption Layer (ApisPeruIdentidadAdapter + IdentidadResponseTranslator)
 * traduce la respuesta JSON de dniruc.apisperu.com a este VO antes de entregarlo
 * al dominio.
 *
 * El campo activo es true si el estado del contribuyente es "ACTIVO" según SUNAT,
 * lo que indica que puede emitir documentos electrónicos.
 */
public record InfoRuc(

        /** Número de RUC de 11 dígitos. */
        String ruc,

        /** Nombre o razón social registrada en SUNAT. */
        String razonSocial,

        /**
         * Estado del contribuyente en SUNAT.
         * Valores habituales: "ACTIVO", "BAJA DE OFICIO", "BAJA PROVISIONAL", etc.
         */
        String estado,

        /**
         * Condición de domicilio fiscal.
         * Valores habituales: "HABIDO", "NO HALLADO", "NO HABIDO", etc.
         */
        String condicion,

        /** Dirección fiscal completa (dirección + distrito + provincia + departamento). */
        String direccion,

        /**
         * true si el estado es "ACTIVO".
         * Simplifica la validación en GuiaService sin que éste conozca
         * el texto exacto devuelto por la API externa.
         */
        boolean activo
) {}
