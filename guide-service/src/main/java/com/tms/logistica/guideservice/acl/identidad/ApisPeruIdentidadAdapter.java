package com.tms.logistica.guideservice.acl.identidad;

import com.tms.logistica.guideservice.acl.identidad.translator.IdentidadResponseTranslator;
import com.tms.logistica.guideservice.domain.port.IdentidadValidadorPort;
import com.tms.logistica.guideservice.domain.valueobject.InfoDni;
import com.tms.logistica.guideservice.domain.valueobject.InfoRuc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * ApisPeruIdentidadAdapter — Adaptador ACL para dniruc.apisperu.com
 *
 * Implementa el puerto IdentidadValidadorPort del dominio utilizando
 * dniruc.apisperu.com como proveedor de validación de RUC (SUNAT) y DNI (RENIEC).
 *
 * PATRÓN: Anti-Corruption Layer (adaptador de salida)
 *
 * Responsabilidades de este adaptador:
 *   1. Construir las URLs con el token JWT como query param (protocolo APIsPerú)
 *   2. Realizar las llamadas HTTP GET
 *   3. Delegar al IdentidadResponseTranslator la conversión de las respuestas
 *      JSON a los value objects InfoRuc e InfoDni del dominio
 *
 * El dominio (GuiaService, ValidacionController) NO conoce esta clase.
 * Si se cambia el proveedor de DNI/RUC, solo se crea un nuevo adaptador
 * sin tocar la lógica de negocio.
 *
 * Autenticación: token JWT configurado en apisperu.dniruc.token.
 * Se envía como query param (?token=...) según el contrato de APIsPerú.
 *
 * Manejo de errores: los métodos retornan Optional.empty() si el recurso
 * no existe (HTTP 404). Para otros errores de red se lanza RuntimeException,
 * que GuiaService captura como best-effort (continúa con datos manuales).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApisPeruIdentidadAdapter implements IdentidadValidadorPort {

    /** Translator que convierte el JSON de APIsPerú a value objects del dominio. */
    private final IdentidadResponseTranslator translator;

    @Value("${apisperu.dniruc.token}")
    private String token;

    @Value("${apisperu.base-url}")
    private String baseUrl;

    @Value("${apisperu.timeout-ms:8000}")
    private int timeoutMs;

    /**
     * Consulta información de un RUC en el padrón de SUNAT vía APIsPerú.
     *
     * @param ruc número de RUC de 11 dígitos
     * @return Optional con InfoRuc si el RUC existe, vacío si no se encuentra
     * @throws RuntimeException si hay error de red o token inválido
     */
    @Override
    public Optional<InfoRuc> validarRuc(String ruc) {
        log.info("[ACL-IDENTIDAD] Consultando RUC: {}", ruc);
        try {
            String urlStr = baseUrl + "/ruc/" + ruc + "?token=" + token;
            String json = get(urlStr);
            log.debug("[ACL-IDENTIDAD] Respuesta RUC {}: {}", ruc, json);
            return Optional.of(translator.toInfoRuc(ruc, json));
        } catch (NotFoundException ex) {
            log.warn("[ACL-IDENTIDAD] RUC {} no encontrado en SUNAT", ruc);
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("[ACL-IDENTIDAD] Error consultando RUC {}: {}", ruc, ex.getMessage());
            throw new RuntimeException("No se pudo validar el RUC " + ruc + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Consulta información de un DNI en el padrón de RENIEC vía APIsPerú.
     *
     * @param dni número de DNI de 8 dígitos
     * @return Optional con InfoDni si el DNI existe, vacío si no se encuentra
     * @throws RuntimeException si hay error de red o token inválido
     */
    @Override
    public Optional<InfoDni> validarDni(String dni) {
        log.info("[ACL-IDENTIDAD] Consultando DNI: {}", dni);
        try {
            String urlStr = baseUrl + "/dni/" + dni + "?token=" + token;
            String json = get(urlStr);
            log.debug("[ACL-IDENTIDAD] Respuesta DNI {}: {}", dni, json);
            return Optional.of(translator.toInfoDni(dni, json));
        } catch (NotFoundException ex) {
            log.warn("[ACL-IDENTIDAD] DNI {} no encontrado en RENIEC", dni);
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("[ACL-IDENTIDAD] Error consultando DNI {}: {}", dni, ex.getMessage());
            throw new RuntimeException("No se pudo validar el DNI " + dni + ": " + ex.getMessage(), ex);
        }
    }

    // ── HTTP Client ─────────────────────────────────────────────────────────

    /**
     * Realiza una petición HTTP GET y retorna el cuerpo de la respuesta.
     * Lanza NotFoundException para HTTP 404 y RuntimeException para otros errores.
     */
    private String get(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);

        int status = conn.getResponseCode();
        if (status == 404) throw new NotFoundException("No encontrado");
        if (status == 401) throw new RuntimeException("Token inválido o expirado");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    /** Excepción interna para distinguir HTTP 404 de otros errores. */
    private static class NotFoundException extends RuntimeException {
        NotFoundException(String msg) { super(msg); }
    }
}
