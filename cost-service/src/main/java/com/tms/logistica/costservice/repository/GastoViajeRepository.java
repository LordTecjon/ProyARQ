package com.tms.logistica.costservice.repository;

import com.tms.logistica.costservice.model.entity.GastoViaje;
import com.tms.logistica.costservice.model.enums.TipoGasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoViajeRepository extends JpaRepository<GastoViaje, Long> {
    List<GastoViaje> findByCostoOrdenId(Long ordenId);
    List<GastoViaje> findByCostoOrdenIdAndTipoGasto(Long ordenId, TipoGasto tipoGasto);
}
