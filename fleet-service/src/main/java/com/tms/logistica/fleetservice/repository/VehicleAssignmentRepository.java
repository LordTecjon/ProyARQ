package com.tms.logistica.fleetservice.repository;

import com.tms.logistica.fleetservice.model.VehicleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleAssignmentRepository
        extends JpaRepository<VehicleAssignment, Long> {
}