package com.tms.logistica.guideservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**
 * SunatGateway - Adaptador para facturacion.apisperu.com
 *
 * Responsabilidad: construir el cuerpo JSON del request, enviarlo al ose
 * (Operador de Servicios Electronicos) de apisperu, y parsear el cdr
 * que devuelve sunat como respuesta.
 *
 * El flujo completo sería
 * guide-service --> facturacion.apisperu.com --> sunat Beta
 *
 * apisperu actua como intermediario: recibe el JSON del TMS, lo convierte
 * al formato XML/SOAP que sunat exige, lo firma digitalmente, y lo envia.
 * luego devuelve el cdr con el codigo de aceptacion o rechazo.
 *
 * adapter + gateway tactica de interoperabilidad "gestionar interfaces".
 * grebuilder esta separado de esta clase para que los cambios en el esquema
 * XSD de sunat solo afecten a grebuilder, sin tocar la logica de comunicacion.
 *
 * Autenticacion: bearer token jwt distinto al de apisperuservice.
 * Se configura en apisperu.facturacion.token en application.properties.
 */
@Slf4j
public class SunatGateway {
    /**
     * Resultado del envio al ose. contiene el codigo cdr de sunat,
     * la descripcion del resultado y la respuesta JSON cruda para auditoria.
     */
    public record CdrResult(
            String codigo,  // "0" = aceptada, "ERROR" = rechazada por el OSE
            String descripcion, // mensaje legible del resultado
            String rawResponse // JSON completo devuelto por APIsPerú para auditoria
    ) {}
    /**
     * URL base del servicio de facturacion.
     * Valor esperado: https://facturacion.apisperu.com/api/v1
     */
    @Value("${apisperu.facturacion.url}")
    private String baseUrl;
    /**
     * Token jwt para autenticar en facturacion.apisperu.com.
     * Se obtiene en el dashboard de facturacion.apisperu.com.
     * Es diferente al token de dniruc.apisperu.com.
     */
    @Value("${apisperu.facturacion.token}")
    private String token;
    /**
     * ID de la empresa registrada en apisperu.
     * Se obtiene al registrar la empresa en facturacion.apisperu.com.
     */
    @Value("${apisperu.facturacion.company-id}")
    private String companyId;
    /**
     * ruc de la empresa emisora registrada en apisperu.
     * En produccion debe coincidir con el ruc real habilitado para gre.
     */
    @Value("${apisperu.facturacion.ruc}")
    private String ruc;
    /**
     * Timeout en ms para la comunicacion con el ose.
     * Mayor que el de dniruc porque sunat puede tardar mas en responder.
     */
    @Value("${apisperu.beta.timeout-ms:10000}")
    private int timeoutMs;

    /**
     * Envia una Guia de Remision Electronica al ose  y retorna el cdr.
     *
     * Este metodo es el punto central de la integracion con sunat.
     * Realiza tres pasos:
     *  1. Construye el JSON con todos los campos requeridos por apisperu
     *  2. Realiza la llamada HTTP POST con autenticacion bearer
     *  3. Parsea la respuesta y extrae el codigo y descripcion del cdr
     *
     * Si hay error de red, lanza RuntimeException para que
     * GuiaService pueda encolar la guia para reenvio automatico.
     *
     * @return CdrResult con el codigo cdr de sunat ("0" = aceptada, "ERROR" =
     * rechazada)
     * @throws "RuntimeException" si hay error de conexion con el ose
     */
    public CdrResult enviar(String serie, String correlativo,
                            String motivoTraslado, String modalidad,
                            String fechaInicio,
                            String remitenteRazon, String remitenteDir, String remitenteUbigeo,
                            String destinatarioRuc, String destinatarioRazon,
                            String destinatarioDir, String destinatarioUbigeo,
                            String destinoDirDetalle, String destinoUbigeo,
                            String vehiculoPlaca,
                            String conductorDni, String conductorNombre, String conductorLicencia,
                            java.util.List<DetalleGuia> detalles) {

        log.info("Enviando guía {}-{} a APIsPerú /despatch/send", serie, correlativo);
        // Paso 1: construir el cuerpo JSON con el formato que exige apisperu
        String json = construirJson(serie, correlativo, motivoTraslado, modalidad,
                fechaInicio, remitenteRazon, remitenteDir, remitenteUbigeo,
                destinatarioRuc, destinatarioRazon, destinatarioDir, destinatarioUbigeo,
                destinoDirDetalle, destinoUbigeo,
                vehiculoPlaca, conductorDni, conductorNombre, conductorLicencia, detalles);

        log.debug("JSON enviado a apisperu: {}", json);

        try {
            // Paso 2: llamada HTTP POST al endpoint /despatch/send del ose
            URL url = new URL(baseUrl + "/despatch/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Autenticacion Bearer: el token identifica la empresa emisora
            // ante el ose
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true); // necesario para enviar cuerpo en POST
            // escritura del JSON en el cuerpo del request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int httpStatus = conn.getResponseCode();
            log.info("HTTP Status APIsPerú: {}", httpStatus);
            // Si httpStatus >= 400, la respuesta de error esta en getErrorStream()
            // Si httpStatus < 300, la respuesta exitosa esta en getInputStream()
            InputStream is = (httpStatus >= 200 && httpStatus < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String rawResponse = leerRespuesta(is);
            log.info("Respuesta APIsPerú: {}", rawResponse);
            // Paso 3: parsear el cdr para extraer codigo y descripcion
            String codigo      = parsearCodigo(rawResponse, httpStatus);
            String descripcion = parsearDescripcion(rawResponse, httpStatus);

            return new CdrResult(codigo, descripcion, rawResponse);

        } catch (IOException ex) {
            // Error de red: se relanza para que GuiaService encole para reenvio
            log.warn("Error de red al contactar APIsPerú: {}", ex.getMessage());
            throw new RuntimeException("Error de conexión con APIsPerú: " + ex.getMessage(), ex);
        }
    }
    /**
     * dto inmutable para los bienes transportados en la guia.
     * separado del dto de la capa de request para no acoplar SunatGateway
     * con los objetos internos del servicio.
     */
    // record para detalle de guía
    public record DetalleGuia(
            String descripcion,
            String unidadMedida ,
            java.math.BigDecimal cantidad,
            java.math.BigDecimal pesoBrutoKg) {}

    // construccion del JSON
    // apisperu espera un JSON con los campos del negocio directamente.
    // Este metodo los mapea desde los parametros del metodo enviar().
    // Se usa String.format() con un texto plano para mantener la legibilidad
    // del JSON sin dependencias adicionales de serializacion.
    private String construirJson(String serie, String correlativo,
                                 String motivoTraslado, String modalidad,
                                 String fechaInicio,
                                 String remitenteRazon, String remitenteDir, String remitenteUbigeo,
                                 String destinatarioRuc, String destinatarioRazon,
                                 String destinatarioDir, String destinatarioUbigeo,
                                 String destinoDirDetalle, String destinoUbigeo,
                                 String vehiculoPlaca,
                                 String conductorDni, String conductorNombre, String conductorLicencia,
                                 java.util.List<DetalleGuia> detalles) {
        // construir el array de items iterando sobre los bienes transportados
        StringBuilder items = new StringBuilder();
        int i = 1;
        for (DetalleGuia d : detalles) {
            if (items.length() > 0) items.append(",");
            items.append(String.format("""
                {
                  "correlativo": %d,
                  "descripcion": "%s",
                  "unidad_medida": "%s",
                  "cantidad": %s,
                  "peso_bruto": %s
                }""", i++, d.descripcion(), d.unidadMedida(),
                    d.cantidad().toPlainString(), d.pesoBrutoKg().toPlainString()));
        }
        // El campo company_id identifica la empresa ante apisperu
        // ruc_emisor es el ruc que aparecera en el documento oficial de sunat
        return String.format("""
                {
                  "company_id": %s,
                  "serie": "%s",
                  "correlativo": "%s",
                  "fecha_emision": "%s",
                  "motivo_traslado": "%s",
                  "modalidad_transporte": "%s",
                  "ruc_emisor": "%s",
                  "razon_social_emisor": "%s",
                  "ubigeo_emisor": "%s",
                  "direccion_emisor": "%s",
                  "ruc_destinatario": "%s",
                  "razon_social_destinatario": "%s",
                  "ubigeo_destinatario": "%s",
                  "direccion_destinatario": "%s",
                  "ubigeo_llegada": "%s",
                  "direccion_llegada": "%s",
                  "placa_vehiculo": "%s",
                  "numero_documento_conductor": "%s",
                  "nombre_conductor": "%s",
                  "numero_licencia_conductor": "%s",
                  "items": [%s]
                }""",
                companyId, serie, correlativo, fechaInicio,
                motivoTraslado, modalidad,
                ruc, remitenteRazon, remitenteUbigeo, remitenteDir,
                destinatarioRuc, destinatarioRazon, destinatarioUbigeo, destinatarioDir,
                destinoUbigeo, destinoDirDetalle,
                vehiculoPlaca, conductorDni, conductorNombre, conductorLicencia,
                items);
    }

    // Helpers
    /**
     * lee todas las lineas del InputStream y las concatena en un string.
     * devuelve cadena vacia si el stream es null .
     */
    private String leerRespuesta(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
    /**
     * extrae el codigo cdr de la respuesta del ose.
     * "0" significa aceptada por sunat; "ERROR" significa rechazada o error
     * del ose.
     */
    private String parsearCodigo(String response, int httpStatus) {
        if (httpStatus == 200 || httpStatus == 201) {
            if (response.contains("\"aceptado\":true")) return "0";
            if (response.contains("\"codigo\":")) {
                return extraer(response, "codigo");
            }
            return "0";
        }
        if (response.contains("\"error\":")) return "ERROR";
        return String.valueOf(httpStatus);
    }
    /**
     * extrae la descripcion legible del resultado del ose.
     * busca en orden: campo "descripcion", "error", "message".
     * para errores conocidos devuelve mensajes predefinidos en espanol.
     */
    private String parsearDescripcion(String response, int httpStatus) {
        if (response.contains("\"descripcion\":")) return extraer(response, "descripcion");
        if (response.contains("\"error\":"))       return extraer(response, "error");
        if (response.contains("\"message\":"))     return extraer(response, "message");
        if (httpStatus == 200 || httpStatus == 201) return "Guia enviada " +
                "exitosamente";
        if (httpStatus == 401) return "token invalido o expirado";
        if (httpStatus == 422) return "datos invalidos para sunat";
        return "HTTP " + httpStatus;
    }
    /**
     * extrae el valor de cadena de un campo en un JSON simple
     * busca el patron "campo":"valor" y devuelve el valor sin comillas.
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
