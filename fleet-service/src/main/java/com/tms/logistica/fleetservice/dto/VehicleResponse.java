package com.tms.logistica.fleetservice.dto;

import com.tms.logistica.fleetservice.model.VehicleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleResponse {

    private Long id;

    private String plate;

    private String chassisNumber;

    private String brand;

    private String model;

    private Integer year;

    private String vehicleType;

    private VehicleStatus status;
}

