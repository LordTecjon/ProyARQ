package com.tms.logistica.customerservice.domain.repository;

import com.tms.logistica.customerservice.domain.entity.Customer;
import com.tms.logistica.customerservice.domain.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByRuc(String ruc);

    Optional<Customer> findByRuc(String ruc);

    // RF4.2 — listado paginado con filtros
    @Query("""
            SELECT c FROM Customer c
            WHERE (:razonSocial IS NULL OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :razonSocial, '%')))
              AND (:ruc        IS NULL OR c.ruc = :ruc)
              AND (:estado     IS NULL OR c.estado = :estado)
            """)
    Page<Customer> findAllWithFilters(
            @Param("razonSocial") String razonSocial,
            @Param("ruc") String ruc,
            @Param("estado") CustomerStatus estado,
            Pageable pageable);

    // RF4.8 — clientes frecuentes (stub: top por conteo de órdenes cuando se integre M1)
    @Query("""
            SELECT c FROM Customer c
            WHERE c.estado = 'ACTIVO'
            ORDER BY c.createdAt DESC
            """)
    List<Customer> findFrequentCustomers(Pageable pageable);

    // Batch — RF batch para M7
    List<Customer> findAllByIdIn(List<UUID> ids);

    // Código único secuencial
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.codigoCliente, 5) AS integer)), 0) FROM Customer c")
    int findMaxCodigoSequence();

    // Verificar si existe tarifa activa en el periodo
    @Query("""
            SELECT COUNT(t) > 0 FROM CustomerTariff t
            WHERE t.customer.id = :clienteId
              AND t.vigenteHasta IS NULL
              AND t.vigentDesde <= :desde
            """)
    boolean existsActiveTariffForPeriod(@Param("clienteId") UUID clienteId,
                                        @Param("desde") LocalDate desde);
}
