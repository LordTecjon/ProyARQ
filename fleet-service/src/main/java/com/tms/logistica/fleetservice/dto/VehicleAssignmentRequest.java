package com.tms.logistica.fleetservice.dto;

import lombok.Data;

@Data
public class VehicleAssignmentRequest {

    private String routeName;

    private Long vehicleId;
}