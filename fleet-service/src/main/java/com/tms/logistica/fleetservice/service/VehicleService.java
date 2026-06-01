package com.tms.logistica.fleetservice.service;

import com.tms.logistica.fleetservice.dto.VehicleRequest;
import com.tms.logistica.fleetservice.dto.VehicleResponse;

import java.util.List;

public interface VehicleService {

    VehicleResponse createVehicle(VehicleRequest request);

    VehicleResponse getVehicleById(Long id);

    List<VehicleResponse> getAllVehicles();

    VehicleResponse updateVehicle(Long id, VehicleRequest request);
}