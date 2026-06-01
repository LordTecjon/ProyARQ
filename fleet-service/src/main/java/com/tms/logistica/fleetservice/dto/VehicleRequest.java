package com.tms.logistica.fleetservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleRequest {

    @NotBlank
    private String plate;

    @NotBlank
    private String chassisNumber;

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    private Integer year;

    private String vehicleType;
}