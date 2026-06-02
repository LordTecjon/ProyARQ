package com.tms.logistica.fleetservice.service;

import java.util.List;
import com.tms.logistica.fleetservice.dto.VehicleDocumentRequest;
import com.tms.logistica.fleetservice.dto.VehicleDocumentResponse;

public interface VehicleDocumentService {

    VehicleDocumentResponse createDocument(
            VehicleDocumentRequest request);
    List<VehicleDocumentResponse> getAllDocuments();
}