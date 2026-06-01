package com.tms.logistica.fleetservice.repository;

import com.tms.logistica.fleetservice.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByPlate(String plate);

    boolean existsByChassisNumber(String chassisNumber);
}