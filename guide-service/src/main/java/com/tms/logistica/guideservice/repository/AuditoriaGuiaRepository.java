package com.tms.logistica.guideservice.repository;

import com.tms.logistica.guideservice.model.entity.AuditoriaGuia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaGuiaRepository extends JpaRepository<AuditoriaGuia, Long> {

    List<AuditoriaGuia> findByGuiaIdOrderByRegistradoEnDesc(Long guiaId);
}
