package com.tms.logistica.customerservice.domain.repository;

import com.tms.logistica.customerservice.domain.entity.CustomerTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerTariffRepository extends JpaRepository<CustomerTariff, UUID> {

    List<CustomerTariff> findByCustomerIdOrderByVigentDesdeDesc(UUID customerId);
}
