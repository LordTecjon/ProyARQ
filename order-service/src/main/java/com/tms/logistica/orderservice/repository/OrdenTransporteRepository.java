package com.tms.logistica.orderservice.repository;

import com.tms.logistica.orderservice.model.entity.OrdenTransporte;
import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenTransporteRepository extends JpaRepository<OrdenTransporte, Long> {
    Optional<OrdenTransporte> findByUuid(String uuid);
    Optional<OrdenTransporte> findByCodigoOrden(String codigoOrden);
    boolean existsByCodigoOrden(String codigoOrden);
    List<OrdenTransporte> findByClienteId(Long clienteId);
    List<OrdenTransporte> findByEstado(EstadoOrden estado);
}
