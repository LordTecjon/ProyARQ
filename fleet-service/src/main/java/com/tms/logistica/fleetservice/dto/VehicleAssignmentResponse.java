package com.tms.logistica.fleetservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class VehicleAssignmentResponse {

    private Long id;

    private String routeName;

    private LocalDate assignmentDate;

    private Long vehicleId;
}