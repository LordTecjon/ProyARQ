package com.tms.logistica.customerservice.domain.repository;

import com.tms.logistica.customerservice.domain.entity.CustomerAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerAuditRepository extends JpaRepository<CustomerAudit, UUID> {

    List<CustomerAudit> findByCustomerIdOrderByModifiedAtDesc(UUID customerId);
}
