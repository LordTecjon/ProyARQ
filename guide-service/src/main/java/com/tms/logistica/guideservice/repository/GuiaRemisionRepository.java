package com.tms.logistica.guideservice.repository;

import com.tms.logistica.guideservice.model.entity.GuiaRemision;
import com.tms.logistica.guideservice.model.enums.EstadoGuia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * GuiaRemisionRepository - Capa de acceso a datos para guias de remision
 *
 * Extiende JpaRepository que provee los metodos CRUD basicos (save, findById,
 * findAll, delete, etc.) sin necesidad de implementarlos.
 *
 * Los metodos derivados (findBy...) son generados automaticamente por Spring Data
 * a partir del nombre del metodo, siguiendo las convenciones de nomenclatura.
 *
 * Las consultas personalizadas usan JPQL (Java Persistence Query Language),
 * que opera sobre entidades Java y no sobre tablas SQL directamente, lo que
 * permite que el codigo sea independiente del motor de base de datos.
 */
@Repository
public interface GuiaRemisionRepository extends JpaRepository<GuiaRemision, Long> {
    /**
     * busca una guia por su UUID publico
     * el UUID es el identificador expuesto en la api rest
     * retorna optional para forzar el manejo explicito del caso "no
     * encontrado"
     */
    Optional<GuiaRemision> findByUuid(String uuid);
    /**
     * lista todas las guias asociadas a una orden de transporte
     * una orden puede tener multiples guias
     */
    List<GuiaRemision> findByOrdenId(Long ordenId);
    /**
     * lista guias por estado util internamente para consultas de
     * administracion
     */
    List<GuiaRemision> findByEstado(EstadoGuia estado);

    /**
     * RF3.8 - historial de guias con filtros opcionales y paginacion
     * todos los parametros son opcionales: si son null, esa condicion se
     * ignora.
     * el patron permite filtros opcionales en jpql.
     * los resultados se ordenan por fecha de creacion descendente
     * @param "fechaDesde"  fecha de inicio minima del traslado
     * @param "fechaHasta"  Fecha de inicio maxima del traslado
     * @param "estado"      estado de la guia a filtrar
     * @param "ordenId"     ID de la orden a filtrar
     * @param "pageable"    configuracion de pagina
     * @return pagina de guias que cumplen los filtros
     */
    @Query("SELECT g FROM GuiaRemision g WHERE " +
            "(:fechaDesde IS NULL OR g.fechaInicio >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR g.fechaInicio <= :fechaHasta) AND " +
            "(:estado IS NULL OR g.estado = :estado) AND " +
            "(:ordenId IS NULL OR g.ordenId = :ordenId) " +
            "ORDER BY g.creadoEn DESC")
    Page<GuiaRemision> buscarConFiltros(
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("estado") EstadoGuia estado,
            @Param("ordenId") Long ordenId,
            Pageable pageable);

    /**
     * cola de reenvio automatico para el ReenvioScheduler.
     *
     * retorna guias que cumplen todas estas condiciones:
     *   - estado PENDIENTE si no fueron aceptadas ni rechazadas definitivamente
     *   - tienen un proximo_reenvio programado no null
     *   - el proximo_reenvio ya vencio es menor o igual a la hora actual
     *   - tienen menos de 5 intentos limite para evitar reintentos
     *   infinitos
     * el scheduler llama a este metodo cada 5 minutos
     */
    @Query("SELECT g FROM GuiaRemision g " +
            "WHERE g.estado = 'PENDIENTE' " +
            "  AND g.proximoReenvio IS NOT NULL " +
            "  AND g.proximoReenvio <= :ahora " +
            "  AND g.intentosEnvio < 5")
    List<GuiaRemision> findPendientesParaReenvio(@Param("ahora") LocalDateTime ahora);
    /**
     * verifica si ya existe una guia con la misma serie y correlativo
     * usado por NumeroGuiaGenerator para validar unicidad antes de asignar
     */
    boolean existsBySerieAndCorrelativo(String serie, String correlativo);
    /**
     * obtiene el correlativo mas alto registrado para una serie dada
     * usado por NumeroGuiaGenerator para calcular el siguiente numero
     * retorna Optional.empty() si no hay guias con esa serie todavia
     */
    @Query("SELECT MAX(g.correlativo) FROM GuiaRemision g WHERE g.serie = :serie")
    Optional<String> findMaxCorrelativoBySerie(@Param("serie") String serie);
}
