package com.tbcare.appointments.repository;

import com.tbcare.appointments.domain.AppointmentRecurrence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRecurrenceRepository extends JpaRepository<AppointmentRecurrence, UUID> {
    List<AppointmentRecurrence> findByTenantIdAndActiveTrue(UUID tenantId);
    Optional<AppointmentRecurrence> findByIdAndTenantId(UUID id, UUID tenantId);
    List<AppointmentRecurrence> findByPatientIdAndTenantIdAndActiveTrue(UUID patientId, UUID tenantId);
    List<AppointmentRecurrence> findByProfessionalIdAndTenantIdAndActiveTrue(UUID professionalId, UUID tenantId);
}
