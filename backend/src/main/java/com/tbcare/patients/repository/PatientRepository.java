package com.tbcare.patients.repository;

import com.tbcare.patients.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findByTenantIdAndActiveTrue(UUID tenantId);
    Optional<Patient> findByIdAndTenantId(UUID id, UUID tenantId);
}
