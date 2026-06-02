package com.tms.logistica.fleetservice.service;

import com.tms.logistica.fleetservice.dto.VehicleAssignmentRequest;
import com.tms.logistica.fleetservice.dto.VehicleAssignmentResponse;
import com.tms.logistica.fleetservice.exception.BusinessException;
import com.tms.logistica.fleetservice.exception.ResourceNotFoundException;
import com.tms.logistica.fleetservice.model.Vehicle;
import com.tms.logistica.fleetservice.model.VehicleAssignment;
import com.tms.logistica.fleetservice.model.VehicleStatus;
import com.tms.logistica.fleetservice.repository.VehicleAssignmentRepository;
import com.tms.logistica.fleetservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VehicleAssignmentServiceImpl
        implements VehicleAssignmentService {

    private final VehicleAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public VehicleAssignmentResponse assignVehicle(
            VehicleAssignmentRequest request) {

        Vehicle vehicle = vehicleRepository.findById(
                        request.getVehicleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehículo no encontrado"));

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessException(
                    "El vehículo no está disponible");
        }

        VehicleAssignment assignment =
                VehicleAssignment.builder()
                        .routeName(request.getRouteName())
                        .assignmentDate(LocalDate.now())
                        .vehicle(vehicle)
                        .build();

        VehicleAssignment saved =
                assignmentRepository.save(assignment);

        vehicle.setStatus(VehicleStatus.ON_ROUTE);
        vehicleRepository.save(vehicle);

        return VehicleAssignmentResponse.builder()
                .id(saved.getId())
                .routeName(saved.getRouteName())
                .assignmentDate(saved.getAssignmentDate())
                .vehicleId(saved.getVehicle().getId())
                .build();
    }
}