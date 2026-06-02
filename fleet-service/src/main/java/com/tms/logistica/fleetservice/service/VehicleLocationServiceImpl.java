package com.tms.logistica.fleetservice.service;

import com.tms.logistica.fleetservice.dto.VehicleLocationRequest;
import com.tms.logistica.fleetservice.dto.VehicleLocationResponse;
import com.tms.logistica.fleetservice.exception.ResourceNotFoundException;
import com.tms.logistica.fleetservice.model.Vehicle;
import com.tms.logistica.fleetservice.model.VehicleLocation;
import com.tms.logistica.fleetservice.repository.VehicleLocationRepository;
import com.tms.logistica.fleetservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleLocationServiceImpl
        implements VehicleLocationService {

    private final VehicleLocationRepository locationRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public VehicleLocationResponse registerLocation(
            VehicleLocationRequest request) {

        Vehicle vehicle = vehicleRepository.findById(
                        request.getVehicleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehículo no encontrado"));

        GeometryFactory geometryFactory =
                new GeometryFactory();

        Point point = geometryFactory.createPoint(
                new Coordinate(
                        request.getLongitude(),
                        request.getLatitude()
                )
        );

        point.setSRID(4326);

        VehicleLocation location =
                VehicleLocation.builder()
                        .location(point)
                        .vehicle(vehicle)
                        .build();

        VehicleLocation saved =
                locationRepository.save(location);

        return VehicleLocationResponse.builder()
                .id(saved.getId())
                .latitude(saved.getLocation().getY())
                .longitude(saved.getLocation().getX())
                .vehicleId(saved.getVehicle().getId())
                .build();
    }

    @Override
    public VehicleLocationResponse getLocationByVehicleId(
            Long vehicleId) {

        VehicleLocation location = locationRepository
                .findByVehicleId(vehicleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ubicación no encontrada"));

        return VehicleLocationResponse.builder()
                .id(location.getId())
                .latitude(location.getLocation().getY())
                .longitude(location.getLocation().getX())
                .vehicleId(location.getVehicle().getId())
                .build();
    }
}