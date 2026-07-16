package com.tms.logistica.orderservice.repository;

import com.tms.logistica.orderservice.model.entity.OrdenTrazabilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenTrazabilidadRepository extends JpaRepository<OrdenTrazabilidad, Long> {
    List<OrdenTrazabilidad> findByOrdenCodigoOrdenOrderByFechaAsc(String codigoOrden);
}
