package com.tms.logistica.fleetservice.controller;

import com.tms.logistica.fleetservice.dto.VehicleLocationRequest;
import com.tms.logistica.fleetservice.dto.VehicleLocationResponse;
import com.tms.logistica.fleetservice.service.VehicleLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class VehicleLocationController {

    private final VehicleLocationService service;

    @PostMapping
    public VehicleLocationResponse registerLocation(
            @RequestBody VehicleLocationRequest request) {

        return service.registerLocation(request);
    }
}