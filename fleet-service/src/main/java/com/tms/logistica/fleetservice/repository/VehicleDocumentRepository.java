package com.tms.logistica.fleetservice.repository;

import com.tms.logistica.fleetservice.model.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleDocumentRepository
        extends JpaRepository<VehicleDocument, Long> {

    List<VehicleDocument> findAllByOrderByExpirationDateAsc();
}