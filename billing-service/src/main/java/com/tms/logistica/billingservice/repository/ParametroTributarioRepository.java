package com.tms.logistica.billingservice.repository;

import com.tms.logistica.billingservice.model.entity.ParametroTributario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametroTributarioRepository extends JpaRepository<ParametroTributario, Long> {
    Optional<ParametroTributario> findFirstByCodigoAndVigenteHastaIsNullOrderByVigenteDesdeDesc(String codigo);
}
