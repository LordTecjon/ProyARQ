package com.tms.logistica.fleetservice.dto;

import lombok.Data;

@Data
public class VehicleLocationRequest {

    private Double latitude;

    private Double longitude;

    private Long vehicleId;
}