package com.tms.logistica.customerservice.domain.repository;

import com.tms.logistica.customerservice.domain.entity.PaymentCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentConditionRepository extends JpaRepository<PaymentCondition, UUID> {

    List<PaymentCondition> findByCustomerIdOrderByVigentDesdeDesc(UUID customerId);

    Optional<PaymentCondition> findFirstByCustomerIdAndVigenteHastaIsNullOrderByVigentDesdeDesc(UUID customerId);
}
