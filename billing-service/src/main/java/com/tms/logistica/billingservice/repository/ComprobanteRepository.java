package com.tms.logistica.billingservice.repository;

import com.tms.logistica.billingservice.model.entity.Comprobante;
import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import com.tms.logistica.billingservice.model.enums.TipoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    Optional<Comprobante> findByUuid(String uuid);
    List<Comprobante> findByClienteId(Long clienteId);
    List<Comprobante> findByEstado(EstadoComprobante estado);
    boolean existsBySerieAndCorrelativo(String serie, String correlativo);
    long countByTipo(TipoComprobante tipo);
}
