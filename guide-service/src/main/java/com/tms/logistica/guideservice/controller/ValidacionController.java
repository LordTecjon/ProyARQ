package com.tms.logistica.guideservice.controller;

import com.tms.logistica.guideservice.domain.port.IdentidadValidadorPort;
import com.tms.logistica.guideservice.domain.valueobject.InfoDni;
import com.tms.logistica.guideservice.domain.valueobject.InfoRuc;
import com.tms.logistica.guideservice.model.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * ValidacionController — Consulta de identidades fiscales y personales
 *
 * Expone endpoints para validar RUC contra el padrón de SUNAT y
 * DNI contra RENIEC.
 *
 * PATRÓN ACL aplicado:
 *   Depende del IdentidadValidadorPort (interfaz del dominio), no de
 *   ApisPeruService (implementación concreta). Los tipos de respuesta son
 *   los value objects del dominio (InfoRuc, InfoDni), sin exponer los
 *   inner records externos de APIsPerú.
 */
@RestController
@RequestMapping("/api/v1/validar")
@RequiredArgsConstructor
public class ValidacionController {

    /** Puerto del ACL para validación de identidades. No conocemos ApisPeruService. */
    private final IdentidadValidadorPort identidadPort;

    /**
     * GET /api/v1/validar/ruc/{ruc}
     * Consulta información de un RUC contra el padrón de SUNAT.
     * Retorna InfoRuc del dominio — no los tipos internos de APIsPerú.
     */
    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<ApiResponse<InfoRuc>> consultarRuc(@PathVariable String ruc) {
        Optional<InfoRuc> info = identidadPort.validarRuc(ruc);
        return info
                .map(i -> ResponseEntity.ok(ApiResponse.ok("ruc consultado", i)))
                .orElseGet(() -> ResponseEntity.ok(
                        ApiResponse.error("RUC " + ruc + " no encontrado en SUNAT")));
    }

    /**
     * GET /api/v1/validar/dni/{dni}
     * Consulta información de un DNI contra RENIEC.
     * Retorna InfoDni del dominio — no los tipos internos de APIsPerú.
     */
    @GetMapping("/dni/{dni}")
    public ResponseEntity<ApiResponse<InfoDni>> consultarDni(@PathVariable String dni) {
        Optional<InfoDni> info = identidadPort.validarDni(dni);
        return info
                .map(i -> ResponseEntity.ok(ApiResponse.ok("dni consultado", i)))
                .orElseGet(() -> ResponseEntity.ok(
                        ApiResponse.error("DNI " + dni + " no encontrado en RENIEC")));
    }
}
