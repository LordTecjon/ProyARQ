package com.tms.logistica.fleetservice.controller;

import java.util.List;
import com.tms.logistica.fleetservice.dto.VehicleDocumentRequest;
import com.tms.logistica.fleetservice.dto.VehicleDocumentResponse;
import com.tms.logistica.fleetservice.service.VehicleDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class VehicleDocumentController {

    private final VehicleDocumentService service;

    @PostMapping
    public VehicleDocumentResponse createDocument(
            @RequestBody VehicleDocumentRequest request) {

        return service.createDocument(request);
    }
    @GetMapping
    public List<VehicleDocumentResponse> getAllDocuments() {

        return service.getAllDocuments();
    }
}

