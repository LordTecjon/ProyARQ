package com.tms.logistica.billingservice.repository;

import com.tms.logistica.billingservice.model.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByComprobanteId(Long comprobanteId);
}
