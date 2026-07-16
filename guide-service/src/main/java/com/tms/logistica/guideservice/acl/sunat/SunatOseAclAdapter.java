package com.tms.logistica.guideservice.acl.sunat;

import com.tms.logistica.guideservice.acl.sunat.translator.SunatResponseTranslator;
import com.tms.logistica.guideservice.domain.port.SunatOsePort;
import com.tms.logistica.guideservice.domain.valueobject.ResultadoEnvioSunat;
import com.tms.logistica.guideservice.model.entity.GuiaDetalle;
import com.tms.logistica.guideservice.model.entity.GuiaRemision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * SunatOseAclAdapter — Adaptador ACL para el OSE de APIsPerú
 *
 * Implementa el puerto SunatOsePort del dominio utilizando
 * facturacion.apisperu.com como OSE intermediario hacia SUNAT Beta.
 *
 * PATRÓN: Anti-Corruption Layer (adaptador de salida)
 *
 * Responsabilidades de este adaptador:
 *   1. Construir el JSON con el formato que exige APIsPerú
 *   2. Realizar la llamada HTTP POST con autenticación Bearer
 *   3. Delegar al SunatResponseTranslator la traducción de la respuesta
 *      al value object ResultadoEnvioSunat del dominio
 *
 * El dominio (GuiaService) NO conoce esta clase — solo conoce SunatOsePort.
 * Si se cambia de OSE (p.ej. de APIsPerú a Nubefact), solo se crea un nuevo
 * adaptador que implemente SunatOsePort, sin tocar GuiaService.
 *
 * Autenticación: Bearer Token JWT configurado en application.properties
 * bajo apisperu.facturacion.token (distinto al token de DNI/RUC).
 *
 * Flujo: guide-service → SunatOseAclAdapter → facturacion.apisperu.com → SUNAT Beta
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SunatOseAclAdapter implements SunatOsePort {

    /** Translator que convierte la respuesta HTTP del OSE al value object del dominio. */
    private final SunatResponseTranslator translator;

    @Value("${apisperu.facturacion.url}")
    private String baseUrl;

    @Value("${apisperu.facturacion.token}")
    private String token;

    @Value("${apisperu.facturacion.company-id}")
    private String companyId;

    @Value("${apisperu.facturacion.ruc}")
    private String rucEmisor;

    @Value("${apisperu.beta.timeout-ms:10000}")
    private int timeoutMs;

    /**
     * Envía la GRE al OSE y retorna el resultado traducido al dominio.
     *
     * Pasos:
     *   1. Extrae los datos necesarios de la entidad GuiaRemision
     *   2. Construye el cuerpo JSON en el formato de APIsPerú
     *   3. Realiza HTTP POST con Bearer token
     *   4. Delega al translator la conversión de la respuesta
     *
     * @param guia entidad con todos los datos de la GRE
     * @return ResultadoEnvioSunat con el CDR traducido al lenguaje del dominio
     * @throws RuntimeException si hay error de red (para que GuiaService encole reintento)
     */
    @Override
    public ResultadoEnvioSunat enviar(GuiaRemision guia) {
        log.info("[ACL-SUNAT] Enviando guía {}-{} a APIsPerú /despatch/send",
                guia.getSerie(), guia.getCorrelativo());

        String json = construirJson(guia);
        log.debug("[ACL-SUNAT] JSON enviado: {}", json);

        try {
            URL url = new URL(baseUrl + "/despatch/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int httpStatus = conn.getResponseCode();
            log.info("[ACL-SUNAT] HTTP Status APIsPerú: {}", httpStatus);

            InputStream is = (httpStatus >= 200 && httpStatus < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String rawResponse = leerRespuesta(is);
            log.info("[ACL-SUNAT] Respuesta APIsPerú: {}", rawResponse);

            // Delegación al translator: aquí ocurre la "anticorrupción"
            // — el dominio recibe ResultadoEnvioSunat, no el JSON crudo
            return translator.traducir(rawResponse, httpStatus);

        } catch (IOException ex) {
            log.warn("[ACL-SUNAT] Error de red al contactar APIsPerú: {}", ex.getMessage());
            throw new RuntimeException("Error de conexión con APIsPerú: " + ex.getMessage(), ex);
        }
    }

    // ── Construcción del JSON ───────────────────────────────────────────────

    /**
     * Construye el cuerpo JSON en el formato que exige APIsPerú para
     * el endpoint /despatch/send. Extrae todos los datos de la entidad GuiaRemision.
     */
    private String construirJson(GuiaRemision guia) {
        List<GuiaDetalle> detalles = guia.getDetalles();
        StringBuilder items = new StringBuilder();
        int i = 1;
        for (GuiaDetalle d : detalles) {
            if (items.length() > 0) items.append(",");
            items.append(formatearItem(i++, d.getDescripcion(), d.getUnidadMedida(),
                    d.getCantidad(), d.getPesoBrutoKg()));
        }

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
                companyId,
                guia.getSerie(), guia.getCorrelativo(),
                guia.getFechaInicio().toString(),
                guia.getMotivoTraslado(), guia.getModalidad(),
                rucEmisor,
                guia.getRemitenteRazon(), guia.getRemitenteUbigeo(), guia.getRemitenteDir(),
                guia.getDestinatarioRuc(), guia.getDestinatarioRazon(),
                guia.getDestinatarioUbigeo(), guia.getDestinatarioDir(),
                guia.getDestinoUbigeo(), guia.getDestinoDir(),
                guia.getVehiculoPlaca(),
                guia.getConductorDni(), guia.getConductorNombre(), guia.getConductorLicencia(),
                items);
    }

    private String formatearItem(int correlativo, String descripcion, String unidadMedida,
                                  BigDecimal cantidad, BigDecimal pesoBrutoKg) {
        return String.format("""
                {
                  "correlativo": %d,
                  "descripcion": "%s",
                  "unidad_medida": "%s",
                  "cantidad": %s,
                  "peso_bruto": %s
                }""", correlativo, descripcion, unidadMedida,
                cantidad.toPlainString(), pesoBrutoKg.toPlainString());
    }

    // ── Utilidad HTTP ───────────────────────────────────────────────────────

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
}
