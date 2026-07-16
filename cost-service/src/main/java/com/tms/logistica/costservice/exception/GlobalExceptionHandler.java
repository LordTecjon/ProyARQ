package com.tms.logistica.costservice.exception;

import com.tms.logistica.costservice.model.dto.response.ApiResponse;
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

    @ExceptionHandler(CostoException.class)
    public ResponseEntity<ApiResponse<Void>> handleCostoException(CostoException ex) {
        log.warn("[{}] {}", ex.getCodigo(), ex.getMessage());
        HttpStatus status = switch (ex.getCodigo()) {
            case "COSTO_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "ESTADO_INVALIDO" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidacion(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error("Datos invalidos: " + errores));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity.internalServerError().body(ApiResponse.error("Error interno del servidor"));
    }
}
