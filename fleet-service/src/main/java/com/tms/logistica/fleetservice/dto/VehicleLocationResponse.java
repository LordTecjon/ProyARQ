package com.tms.logistica.fleetservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleLocationResponse {

    private Long id;

    private Double latitude;

    private Double longitude;

    private Long vehicleId;
}