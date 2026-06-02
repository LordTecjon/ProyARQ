package com.tms.logistica.fleetservice.repository;

import com.tms.logistica.fleetservice.model.VehicleLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleLocationRepository
        extends JpaRepository<VehicleLocation, Long> {
}