package com.tms.logistica.guideservice.exception;

import com.tms.logistica.guideservice.model.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(GuiaException.class)
    public ResponseEntity<ApiResponse<Void>> handleGuiaException(GuiaException ex) {
        log.warn("[{}] {}", ex.getCodigo(), ex.getMessage());
        HttpStatus status = switch (ex.getCodigo()) {
            case "GUIA_NOT_FOUND"   -> HttpStatus.NOT_FOUND;
            case "ESTADO_INVALIDO"  -> HttpStatus.CONFLICT;
            case "VALIDACION_SUNAT" -> HttpStatus.UNPROCESSABLE_ENTITY;
            default                 -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidacion(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Datos inválidos: " + errores));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error interno del servidor"));
    }
}
