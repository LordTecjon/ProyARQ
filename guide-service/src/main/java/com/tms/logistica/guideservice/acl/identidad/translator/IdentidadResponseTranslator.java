package com.tms.logistica.guideservice.acl.identidad.translator;

import com.tms.logistica.guideservice.domain.valueobject.InfoDni;
import com.tms.logistica.guideservice.domain.valueobject.InfoRuc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IdentidadResponseTranslator — Traductor del ACL para respuestas de APIsPerú DNI/RUC
 *
 * Responsabilidad: convertir las respuestas JSON crudas de dniruc.apisperu.com
 * a los value objects InfoRuc e InfoDni del dominio.
 *
 * PATRÓN: Anti-Corruption Layer — Translator
 * Este componente aísla al dominio de la estructura particular del JSON
 * de APIsPerú para RUC y DNI. Si APIsPerú cambia los nombres de campos
 * en su respuesta, solo se modifica este translator.
 *
 * Decisión de diseño: el parser es manual con indexOf() para mantener
 * coherencia con el resto del proyecto (sin añadir Jackson u otra librería
 * de serialización exclusivamente para este propósito).
 */
@Component
@Slf4j
public class IdentidadResponseTranslator {

    /**
     * Traduce la respuesta JSON de APIsPerú para RUC al value object InfoRuc.
     *
     * La API devuelve campos separados (departamento, provincia, distrito)
     * que se concatenan para formar una dirección completa y legible.
     *
     * @param ruc  número de RUC consultado (se usa para poblar el record)
     * @param json respuesta JSON completa de APIsPerú /ruc/{ruc}
     * @return InfoRuc con los datos del contribuyente en términos del dominio
     */
    public InfoRuc toInfoRuc(String ruc, String json) {
        log.debug("Traduciendo respuesta RUC de APIsPerú para ruc={}", ruc);

        String razonSocial  = extraer(json, "razonSocial");
        String estado       = extraer(json, "estado");
        String condicion    = extraer(json, "condicion");
        String direccion    = extraer(json, "direccion");
        String departamento = extraer(json, "departamento");
        String provincia    = extraer(json, "provincia");
        String distrito     = extraer(json, "distrito");

        // La API devuelve dirección, distrito, provincia y departamento por separado.
        // Se concatenan para obtener una cadena legible como dirección completa.
        String direccionCompleta = direccion
                + " - " + distrito
                + ", " + provincia
                + ", " + departamento;

        // "ACTIVO" es el estado que indica que el contribuyente puede operar
        boolean activo = "ACTIVO".equalsIgnoreCase(estado);

        return new InfoRuc(ruc, razonSocial, estado, condicion, direccionCompleta, activo);
    }

    /**
     * Traduce la respuesta JSON de APIsPerú para DNI al value object InfoDni.
     *
     * Construye el nombre completo en el orden estándar peruano:
     * "{apellidoPaterno} {apellidoMaterno} {nombres}".
     *
     * @param dni  número de DNI consultado
     * @param json respuesta JSON completa de APIsPerú /dni/{dni}
     * @return InfoDni con los datos del ciudadano en términos del dominio
     */
    public InfoDni toInfoDni(String dni, String json) {
        log.debug("Traduciendo respuesta DNI de APIsPerú para dni={}", dni);

        String nombres         = extraer(json, "nombres");
        String apellidoPaterno = extraer(json, "apellidoPaterno");
        String apellidoMaterno = extraer(json, "apellidoMaterno");

        // Formato estándar en documentos oficiales peruanos: apellidos primero
        String nombreCompleto = (apellidoPaterno + " " + apellidoMaterno + " " + nombres).trim();

        return new InfoDni(dni, nombres, apellidoPaterno, apellidoMaterno, nombreCompleto);
    }

    // ── Utilidad de parseo JSON manual ──────────────────────────────────────

    /**
     * Extrae el valor string de un campo en un JSON plano.
     * Busca el patrón "campo":"valor" y retorna el valor sin comillas.
     * Retorna cadena vacía si el campo no existe en el JSON.
     */
    private String extraer(String json, String campo) {
        String clave = "\"" + campo + "\":\"";
        int ini = json.indexOf(clave);
        if (ini < 0) return "";
        ini += clave.length();
        int fin = json.indexOf("\"", ini);
        return fin > ini ? json.substring(ini, fin) : "";
    }
}
