package com.tms.logistica.billingservice.exception;

import com.tms.logistica.billingservice.model.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<ApiResponse<Void>> handleBillingException(BillingException ex) {
        log.warn("[{}] {}", ex.getCodigo(), ex.getMessage());
        HttpStatus status = switch (ex.getCodigo()) {
            case "COMPROBANTE_NOT_FOUND", "CLIENTE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "ESTADO_INVALIDO", "PAGO_INVALIDO" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de concurrencia: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("El comprobante fue modificado. Recargue e intente nuevamente"));
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
