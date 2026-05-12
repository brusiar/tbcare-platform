package com.tbcare.appointments.repository;

import com.tbcare.appointments.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByTenantIdAndScheduledAtBetween(UUID tenantId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByTenantIdAndPatientId(UUID tenantId, UUID patientId);
    Optional<Appointment> findByIdAndTenantId(UUID id, UUID tenantId);
}
