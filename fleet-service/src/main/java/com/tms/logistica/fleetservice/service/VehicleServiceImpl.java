package com.tms.logistica.fleetservice.service;

import com.tms.logistica.fleetservice.dto.VehicleRequest;
import com.tms.logistica.fleetservice.dto.VehicleResponse;
import com.tms.logistica.fleetservice.exception.BusinessException;
import com.tms.logistica.fleetservice.exception.ResourceNotFoundException;
import com.tms.logistica.fleetservice.model.Vehicle;
import com.tms.logistica.fleetservice.model.VehicleStatus;
import com.tms.logistica.fleetservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repository;

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {

        if (repository.existsByPlate(request.getPlate())) {
            throw new BusinessException("La placa ya existe");
        }

        if (repository.existsByChassisNumber(request.getChassisNumber())) {
            throw new BusinessException("El número de chasis ya existe");
        }

        Vehicle vehicle = Vehicle.builder()
                .plate(request.getPlate())
                .chassisNumber(request.getChassisNumber())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .vehicleType(request.getVehicleType())
                .status(VehicleStatus.AVAILABLE)
                .build();

        Vehicle saved = repository.save(vehicle);

        return mapToResponse(saved);
    }

    @Override
    public VehicleResponse getVehicleById(Long id) {

        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vehículo no encontrado"));

        return mapToResponse(vehicle);
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {

        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vehículo no encontrado"));

        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setVehicleType(request.getVehicleType());

        Vehicle updated = repository.save(vehicle);

        return mapToResponse(updated);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plate(vehicle.getPlate())
                .chassisNumber(vehicle.getChassisNumber())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .vehicleType(vehicle.getVehicleType())
                .status(vehicle.getStatus())
                .build();
    }
}