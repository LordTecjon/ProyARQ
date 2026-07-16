package com.tms.logistica.guideservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @deprecated Reemplazado por el patrón Anti-Corruption Layer.
 *
 * Esta clase fue sustituida por:
 *   - {@link com.tms.logistica.guideservice.domain.port.IdentidadValidadorPort}
 *     (interfaz/port del dominio)
 *   - {@link com.tms.logistica.guideservice.acl.identidad.ApisPeruIdentidadAdapter}
 *     (implementación ACL que encapsula la comunicación con APIsPerú DNI/RUC)
 *   - {@link com.tms.logistica.guideservice.acl.identidad.translator.IdentidadResponseTranslator}
 *     (translator que convierte el JSON de APIsPerú a value objects del dominio)
 *   - {@link com.tms.logistica.guideservice.domain.valueobject.InfoRuc} e
 *     {@link com.tms.logistica.guideservice.domain.valueobject.InfoDni}
 *     (value objects del dominio que reemplazan los inner records RucInfo y DniInfo)
 *
 * GuiaService y ValidacionController ya NO dependen de esta clase.
 * Este archivo puede eliminarse en la siguiente limpieza de código.
 */
@Deprecated(since = "ACL refactoring", forRemoval = true)
@Service
@Slf4j
public class ApisPeruService {
    /**
     * Token JWT para autenticar en dniruc.apisperu.com
     * Se obtiene en jwt.apisperu.com. Es diferente al token de facturacion
     * Se inyecta desde application.properties para no hardcodear credenciales
     */
    @Value("${apisperu.dniruc.token}")
    private String token;
    /**
     * url base de la api configurable para cambiar de proveedor sin
     * recompilar
     * valor esperado: https://dniruc.apisperu.com/api/v1
     */
    @Value("${apisperu.base-url}")
    private String baseUrl;
    /**
     * tiempo maximo de espera en milisegundos para conexion y lectura
     * valor por defecto: 8000 ms. Evita que un timeout del proveedor externo
     * bloquee el hilo del servidor indefinidamente
     */
    @Value("${apisperu.timeout-ms:8000}")
    private int timeoutMs;
    /**
     * datos de un contribuyente segun el padron de sunat
     * se usa un record porque es un dto inmutable de solo lectura
     */
    public record RucInfo(
            String ruc,
            String razonSocial,
            String estado,
            String condicion,
            String direccion,
            boolean activo
    ) {}
    /**
     * datos de una persona segun el padron de reniec
     * nombreCompleto se construye concatenando apellidos y nombres en orden
     * peruano
     */
    public record DniInfo(
            String dni,
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            String nombreCompleto  // formato: apellidoPaterno
            // apellidoMaterno  nombres
    ) {}

    /**
     * consulta información de un ruc.
     * lanza excepción si el ruc no existe o está inactivo.
     */
    public RucInfo consultarRuc(String ruc) {
        log.info("consultando ruc: {}", ruc);
        try {
            // El token va como query parameter segun el contrato de la API de APIsPeru
            String urlStr = baseUrl + "/ruc/" + ruc + "?token=" + token;
            String response = get(urlStr);
            log.debug("respuesta RUC {}: {}", ruc, response);
            return parsearRuc(ruc, response);
        } catch (Exception ex) {
            log.warn("error consultando ruc {}: {}", ruc, ex.getMessage());
            throw new RuntimeException("no se pudo validar el RUC " + ruc +
                    ": " + ex.getMessage(), ex);
        }
    }

    /**
     * consulta los datos de un dni contra el padron de reniec.
     *
     * uso principal: GuiaService llama a este metodo para obtener el nombre
     * oficial del conductor
     * si la API responde, el nombre oficial reemplaza al ingresado manualmente.
     *
     * @param "dni" numero de dni de 8 digitos
     * @return dniInfo con nombres y apellidos oficiales de reniec
     * @throws "RuntimeException" si la api no responde o el dni no existe
     */
    public DniInfo consultarDni(String dni) {
        log.info("consultando dni: {}", dni);
        try {
            String urlStr = baseUrl + "/dni/" + dni + "?token=" + token;
            String response = get(urlStr);
            log.debug("respuesta dni {}: {}", dni, response);
            return parsearDni(dni, response);
        } catch (Exception ex) {
            log.warn("error consultando dni {}: {}", dni, ex.getMessage());
            throw new RuntimeException("no se pudo validar el dni " + dni +
                    ": " + ex.getMessage(), ex);
        }
    }

    // Peticion HTTP GET
    // se usa HttpURLConnection nativa de java sin dependencias adicionales
    // la api de apisperu es simple: get con token en query param, responde json
    // no se usa RestTemplate ni WebClient para mantener el proyecto liviano

    private String get(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(timeoutMs); // limite para establecer la
        // conexion tcp
        conn.setReadTimeout(timeoutMs); // limite para recibir el primer byte
        // de respuesta

        int status = conn.getResponseCode();

        // tratamiento de errores HTTP antes de leer el cuerpo de la respuesta
        if (status == 404) throw new RuntimeException("No encontrado");
        if (status == 401) throw new RuntimeException("Token inválido o expirado");

        // lectura del cuerpo de la respuesta linea por linea en UTF-8
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }


    // Parsers JSON manuales
    // se evita jackson u otra libreria para no agregar dependencias
    // innecesarias
    // la respuesta de apisperu es un JSON plano, por lo que el parser manual
    // con indexOf es suficiente y mas liviano

    /**
     * extrae los campos del json de ruc y construye el objeto rucinfo
     * combina los campos geograficos separados para formar la direccion
     * completa
     */
    private RucInfo parsearRuc(String ruc, String json) {
        String razonSocial = extraer(json, "razonSocial");
        String estado      = extraer(json, "estado");
        String condicion   = extraer(json, "condicion");
        String direccion   = extraer(json, "direccion");
        String departamento = extraer(json, "departamento");
        String provincia   = extraer(json, "provincia");
        String distrito    = extraer(json, "distrito");
        // si sunat devuelve "ACTIVO" el contribuyente puede emitir documentos
        boolean activo     = "ACTIVO".equalsIgnoreCase(estado);

        // la api devuelve direccion, distrito, provincia y departamento por
        // separado
        // se concatenan para tener una sola cadena legible

        return new RucInfo(ruc, razonSocial, estado, condicion,
                direccion + " - " + distrito + ", " + provincia + ", " + departamento,
                activo);
    }
    /**
     * extrae los campos del json de dni y construye el objeto dniinfo.
     * el nombre completo se forma en el orden estandar peruano: apellidos
     * primero.
     */

    private DniInfo parsearDni(String dni, String json) {
        String nombres          = extraer(json, "nombres");
        String apellidoPaterno  = extraer(json, "apellidoPaterno");
        String apellidoMaterno  = extraer(json, "apellidoMaterno");
        // orden estandar en documentos oficiales peruanos: apellidoPaterno
        // apellidoMaterno nombres
        String nombreCompleto   = (apellidoPaterno + " " + apellidoMaterno + " " + nombres).trim();
        return new DniInfo(dni, nombres, apellidoPaterno, apellidoMaterno, nombreCompleto);
    }
    /**
     * extrae el valor de cadena de un campo en un JSON simple.
     * busca el patron "campo":"valor" y devuelve el valor sin comillas.
     * devuelve cadena vacia si el campo no existe.
     */
    private String extraer(String json, String campo) {
        String clave = "\"" + campo + "\":\"";
        int ini = json.indexOf(clave);
        if (ini < 0) return ""; // campo no encontrado en el json
        ini += clave.length();
        int fin = json.indexOf("\"", ini);
        return fin > ini ? json.substring(ini, fin) : "";
    }
}
