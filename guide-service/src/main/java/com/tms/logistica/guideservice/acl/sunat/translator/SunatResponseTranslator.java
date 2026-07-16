package com.tms.logistica.guideservice.acl.sunat.translator;

import com.tms.logistica.guideservice.domain.valueobject.ResultadoEnvioSunat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SunatResponseTranslator — Traductor del ACL para respuestas de APIsPerú/SUNAT
 *
 * Responsabilidad: convertir la respuesta JSON cruda del OSE (APIsPerú)
 * al value object ResultadoEnvioSunat del dominio.
 *
 * PATRÓN: Anti-Corruption Layer — Translator
 * Este componente es el núcleo de la "capa anticorrupción" para SUNAT:
 * aísla al dominio de la estructura particular del JSON de APIsPerú.
 * Si APIsPerú cambia su contrato de respuesta, solo se modifica este translator.
 *
 * Lógica de traducción:
 *   - HTTP 200/201 + "aceptado":true  → codigoCdr="0", aceptada=true
 *   - HTTP 200/201 + campo "codigo"   → extrae el código CDR literal
 *   - HTTP 4xx/5xx                    → codigoCdr="ERROR"
 *
 * El parser es manual con indexOf() para mantener el proyecto sin dependencias
 * adicionales de serialización JSON (coherente con la decisión en ApisPeruService).
 */
@Component
@Slf4j
public class SunatResponseTranslator {

    /**
     * Traduce la respuesta HTTP del OSE al value object del dominio.
     *
     * @param rawResponse cuerpo JSON completo de la respuesta del OSE
     * @param httpStatus  código HTTP de la respuesta (200, 201, 401, 422, etc.)
     * @return ResultadoEnvioSunat con los datos normalizados al lenguaje del dominio
     */
    public ResultadoEnvioSunat traducir(String rawResponse, int httpStatus) {
        log.debug("Traduciendo respuesta OSE — HTTP {}: {}", httpStatus, rawResponse);

        String codigo      = extraerCodigo(rawResponse, httpStatus);
        String descripcion = extraerDescripcion(rawResponse, httpStatus);
        boolean aceptada   = "0".equals(codigo);

        log.info("CDR traducido — código: {}, aceptada: {}, descripción: {}",
                codigo, aceptada, descripcion);

        return new ResultadoEnvioSunat(codigo, descripcion, aceptada, rawResponse);
    }

    // ── Lógica de extracción del código CDR ────────────────────────────────

    /**
     * Extrae el código CDR de SUNAT de la respuesta del OSE.
     * "0" significa que SUNAT aceptó la GRE.
     * Cualquier otro valor es un código de rechazo o error.
     */
    private String extraerCodigo(String response, int httpStatus) {
        if (httpStatus == 200 || httpStatus == 201) {
            // APIsPerú puede devolver "aceptado":true para indicar éxito
            if (response.contains("\"aceptado\":true")) return "0";
            // o puede devolver un campo "codigo" con el código CDR literal de SUNAT
            if (response.contains("\"codigo\":")) {
                return extraer(response, "codigo");
            }
            // 200 sin campos conocidos: se asume aceptada
            return "0";
        }
        // En caso de error HTTP, se busca el campo "error" en el body
        if (response.contains("\"error\":")) return "ERROR";
        // Fallback: se devuelve el código HTTP como string
        return String.valueOf(httpStatus);
    }

    /**
     * Extrae la descripción legible del resultado del OSE.
     * Busca en orden: "descripcion" → "error" → "message" → texto predeterminado.
     */
    private String extraerDescripcion(String response, int httpStatus) {
        if (response.contains("\"descripcion\":")) return extraer(response, "descripcion");
        if (response.contains("\"error\":"))       return extraer(response, "error");
        if (response.contains("\"message\":"))     return extraer(response, "message");
        // Textos predeterminados para códigos HTTP conocidos
        if (httpStatus == 200 || httpStatus == 201) return "Guia enviada exitosamente";
        if (httpStatus == 401) return "Token invalido o expirado";
        if (httpStatus == 422) return "Datos invalidos para SUNAT";
        return "HTTP " + httpStatus;
    }

    // ── Utilidad de parseo JSON manual ──────────────────────────────────────

    /**
     * Extrae el valor string de un campo en un JSON simple.
     * Busca el patrón "campo":"valor" y retorna el valor sin comillas.
     * Retorna cadena vacía si el campo no existe.
     */
    private String extraer(String json, String campo) {
        String clave = "\"" + campo + "\":\"";
        int ini = json.indexOf(clave);
        if (ini < 0) return "";
        ini += clave.length();
        int fin = json.indexOf("\"", ini);
        return fin > ini ? json.substring(ini, fin).trim() : "";
    }
}
