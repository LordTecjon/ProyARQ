package com.tms.logistica.guideservice.controller;

import com.tms.logistica.guideservice.model.dto.request.CrearGuiaRequest;
import com.tms.logistica.guideservice.model.dto.request.FiltroGuiaRequest;
import com.tms.logistica.guideservice.model.dto.response.ApiResponse;
import com.tms.logistica.guideservice.model.dto.response.GuiaResponse;
import com.tms.logistica.guideservice.model.dto.response.PaginaResponse;
import com.tms.logistica.guideservice.service.GuiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guias")
@RequiredArgsConstructor
public class GuiaController {

    private final GuiaService guiaService;

    private static final String HEADER_USUARIO = "X-Usuario";
    private static final String USUARIO_DEFAULT = "sistema";

    // RF3.1 Crear guía
    @PostMapping
    public ResponseEntity<ApiResponse<GuiaResponse>> crear(
            @Valid @RequestBody CrearGuiaRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        GuiaResponse response = guiaService.crearGuia(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("guia creada exitosamente", response));
    }

    // RF3.4 Consultar guía por UUID
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<GuiaResponse>> obtener(@PathVariable String uuid) {
        return ResponseEntity.ok(
                ApiResponse.ok("guia encontrada",
                        guiaService.obtenerPorUuid(uuid)));
    }

    // RF3.6 Listar guías por orden
    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<ApiResponse<List<GuiaResponse>>> listarPorOrden(@PathVariable Long ordenId) {
        return ResponseEntity.ok(
                ApiResponse.ok("guias de la orden " + ordenId,
                        guiaService.listarPorOrden(ordenId)));
    }

    // RF3.8 Historial con filtros y paginacion
    @GetMapping("/historial")
    public ResponseEntity<ApiResponse<PaginaResponse<GuiaResponse>>> historial(
            FiltroGuiaRequest filtro) {
        PaginaResponse<GuiaResponse> pagina = guiaService.listarHistorial(filtro);
        return ResponseEntity.ok(ApiResponse.ok("historial de guias", pagina));
    }

    // RF3.3 Enviar a sunat
    @PostMapping("/{uuid}/enviar")
    public ResponseEntity<ApiResponse<GuiaResponse>> enviar(
            @PathVariable String uuid,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        GuiaResponse response = guiaService.enviarASunat(uuid, usuario);
        return ResponseEntity.ok(ApiResponse.ok("guia enviada a sunat",
                response));
    }

    // RF3.7 Anular guia
    @PostMapping("/{uuid}/anular")
    public ResponseEntity<ApiResponse<GuiaResponse>> anular(
            @PathVariable String uuid,
            @RequestParam String motivo,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        GuiaResponse response = guiaService.anularGuia(uuid, motivo, usuario);
        return ResponseEntity.ok(ApiResponse.ok("guia anulada", response));
    }

    // RF3.5 Descargar PDF
    @GetMapping("/{uuid}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable String uuid) {
        byte[] pdf = guiaService.descargarPdf(uuid);
        String nombre = guiaService.nombrePdf(uuid);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(nombre).build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
