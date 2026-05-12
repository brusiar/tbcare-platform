package com.tbcare.professionals.repository;

import com.tbcare.professionals.domain.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {
    List<Professional> findByTenantIdAndActiveTrue(UUID tenantId);
    Optional<Professional> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Professional> findByUserIdAndTenantId(UUID userId, UUID tenantId);
}
