package com.tms.logistica.fleetservice.controller;

import com.tms.logistica.fleetservice.dto.VehicleRequest;
import com.tms.logistica.fleetservice.dto.VehicleResponse;
import com.tms.logistica.fleetservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @PostMapping
    public VehicleResponse createVehicle(
            @Valid @RequestBody VehicleRequest request) {

        return service.createVehicle(request);
    }

    @GetMapping("/{id}")
    public VehicleResponse getVehicleById(
            @PathVariable Long id) {

        return service.getVehicleById(id);
    }

    @GetMapping
    public List<VehicleResponse> getAllVehicles() {

        return service.getAllVehicles();
    }

    @PutMapping("/{id}")
    public VehicleResponse updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request) {

        return service.updateVehicle(id, request);
    }
}
