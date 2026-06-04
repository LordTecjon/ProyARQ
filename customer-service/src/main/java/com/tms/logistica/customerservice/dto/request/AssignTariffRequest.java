package com.tms.logistica.customerservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AssignTariffRequest {

    @NotNull(message = "El ID de tarifa es obligatorio")
    private UUID tarifaId;

    @NotNull(message = "La fecha de vigencia es obligatoria")
    private LocalDate vigentDesde;

    private LocalDate vigenteHasta;
}
