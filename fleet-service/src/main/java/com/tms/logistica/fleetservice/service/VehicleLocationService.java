package com.tms.logistica.fleetservice.service;

import com.tms.logistica.fleetservice.dto.VehicleLocationRequest;
import com.tms.logistica.fleetservice.dto.VehicleLocationResponse;

public interface VehicleLocationService {

    VehicleLocationResponse registerLocation(
            VehicleLocationRequest request);
}