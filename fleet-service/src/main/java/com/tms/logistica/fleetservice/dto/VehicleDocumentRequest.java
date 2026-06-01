package com.tms.logistica.fleetservice.dto;

import com.tms.logistica.fleetservice.model.VehicleDocumentType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleDocumentRequest {

    private String documentNumber;

    private VehicleDocumentType documentType;

    private LocalDate issueDate;

    private LocalDate expirationDate;

    private Long vehicleId;
}