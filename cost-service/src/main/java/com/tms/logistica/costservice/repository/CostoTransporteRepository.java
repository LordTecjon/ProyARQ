package com.tms.logistica.costservice.repository;

import com.tms.logistica.costservice.model.entity.CostoTransporte;
import com.tms.logistica.costservice.model.enums.EstadoCosto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CostoTransporteRepository extends JpaRepository<CostoTransporte, Long> {
    Optional<CostoTransporte> findByUuid(String uuid);
    Optional<CostoTransporte> findFirstByOrdenId(Long ordenId);
    List<CostoTransporte> findByOrdenId(Long ordenId);
    List<CostoTransporte> findByEstado(EstadoCosto estado);
}
