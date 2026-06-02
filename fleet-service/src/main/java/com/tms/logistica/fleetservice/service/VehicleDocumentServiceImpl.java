package com.tms.logistica.fleetservice.service;

import java.util.List;
import com.tms.logistica.fleetservice.dto.VehicleDocumentRequest;
import com.tms.logistica.fleetservice.dto.VehicleDocumentResponse;
import com.tms.logistica.fleetservice.exception.ResourceNotFoundException;
import com.tms.logistica.fleetservice.model.Vehicle;
import com.tms.logistica.fleetservice.model.VehicleDocument;
import com.tms.logistica.fleetservice.repository.VehicleDocumentRepository;
import com.tms.logistica.fleetservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleDocumentServiceImpl
        implements VehicleDocumentService {

    private final VehicleDocumentRepository documentRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public VehicleDocumentResponse createDocument(
            VehicleDocumentRequest request) {

        Vehicle vehicle = vehicleRepository.findById(
                        request.getVehicleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehículo no encontrado"));

        VehicleDocument document = VehicleDocument.builder()
                .documentNumber(request.getDocumentNumber())
                .documentType(request.getDocumentType())
                .issueDate(request.getIssueDate())
                .expirationDate(request.getExpirationDate())
                .vehicle(vehicle)
                .build();

        VehicleDocument saved =
                documentRepository.save(document);

        return VehicleDocumentResponse.builder()
                .id(saved.getId())
                .documentNumber(saved.getDocumentNumber())
                .documentType(saved.getDocumentType())
                .issueDate(saved.getIssueDate())
                .expirationDate(saved.getExpirationDate())
                .vehicleId(saved.getVehicle().getId())
                .build();
    }
    @Override
    public List<VehicleDocumentResponse> getAllDocuments() {

        return documentRepository
                .findAllByOrderByExpirationDateAsc()
                .stream()
                .map(document ->
                        VehicleDocumentResponse.builder()
                                .id(document.getId())
                                .documentNumber(document.getDocumentNumber())
                                .documentType(document.getDocumentType())
                                .issueDate(document.getIssueDate())
                                .expirationDate(document.getExpirationDate())
                                .vehicleId(document.getVehicle().getId())
                                .build())
                .toList();
    }
}