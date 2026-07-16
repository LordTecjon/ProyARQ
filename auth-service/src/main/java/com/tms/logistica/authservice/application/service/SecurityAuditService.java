package com.tms.logistica.authservice.application.service;

import com.tms.logistica.authservice.domain.entity.AuditEvent;
import com.tms.logistica.authservice.domain.entity.User;
import com.tms.logistica.authservice.domain.enums.AuditEventType;
import com.tms.logistica.authservice.domain.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final AuditEventRepository auditEventRepository;

    @Async
    public void log(User usuario, AuditEventType tipo, String modulo, String detalle, String ip) {
        auditEventRepository.save(AuditEvent.builder()
                .usuario(usuario)
                .tipoAccion(tipo)
                .modulo(modulo)
                .detalle(detalle)
                .ipOrigen(ip)
                .build());
    }

    @Async
    public void log(AuditEventType tipo, String modulo, String detalle, String ip) {
        log(null, tipo, modulo, detalle, ip);
    }
}
