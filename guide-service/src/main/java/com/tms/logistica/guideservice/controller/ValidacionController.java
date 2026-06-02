package com.tms.logistica.guideservice.controller;

import com.tms.logistica.guideservice.model.dto.response.ApiResponse;
import com.tms.logistica.guideservice.service.ApisPeruService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/validar")
@RequiredArgsConstructor
public class ValidacionController {

    private final ApisPeruService apisPeruService;

    /**
     * GET /api/v1/validar/ruc/{ruc}
     * consulta informacion de un RUC contra apisperu.
     */
    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<ApiResponse<ApisPeruService.RucInfo>> consultarRuc(
            @PathVariable String ruc) {
        ApisPeruService.RucInfo info = apisPeruService.consultarRuc(ruc);
        return ResponseEntity.ok(ApiResponse.ok("ruc consultado", info));
    }

    /**
     * GET /api/v1/validar/dni/{dni}
     * consulta informacion de un dni contra apis apisperu.
     */
    @GetMapping("/dni/{dni}")
    public ResponseEntity<ApiResponse<ApisPeruService.DniInfo>> consultarDni(
            @PathVariable String dni) {
        ApisPeruService.DniInfo info = apisPeruService.consultarDni(dni);
        return ResponseEntity.ok(ApiResponse.ok("dni consultado", info));
    }
}
