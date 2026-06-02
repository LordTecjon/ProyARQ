package com.tms.logistica.fleetservice.service;

import com.tms.logistica.fleetservice.dto.VehicleAssignmentRequest;
import com.tms.logistica.fleetservice.dto.VehicleAssignmentResponse;

public interface VehicleAssignmentService {

    VehicleAssignmentResponse assignVehicle(
            VehicleAssignmentRequest request);
}