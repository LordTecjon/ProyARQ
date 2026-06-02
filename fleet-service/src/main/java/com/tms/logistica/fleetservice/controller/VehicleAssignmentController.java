package com.tms.logistica.fleetservice.controller;

import com.tms.logistica.fleetservice.dto.VehicleAssignmentRequest;
import com.tms.logistica.fleetservice.dto.VehicleAssignmentResponse;
import com.tms.logistica.fleetservice.service.VehicleAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class VehicleAssignmentController {

    private final VehicleAssignmentService service;

    @PostMapping
    public VehicleAssignmentResponse assignVehicle(
            @RequestBody VehicleAssignmentRequest request) {

        return service.assignVehicle(request);
    }
}