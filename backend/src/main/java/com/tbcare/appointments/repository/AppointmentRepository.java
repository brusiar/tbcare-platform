package com.tbcare.appointments.repository;

import com.tbcare.appointments.domain.Appointment;
import com.tbcare.appointments.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByTenantIdAndScheduledAtBetween(UUID tenantId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByTenantIdAndPatientId(UUID tenantId, UUID patientId);
    Optional<Appointment> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT a FROM Appointment a WHERE a.tenantId = :tenantId " +
           "AND a.professionalId = :professionalId " +
           "AND a.status != :cancelledStatus " +
           "AND a.id != :excludeId " +
           "AND ((a.scheduledAt < :endTime AND FUNCTION('TIMESTAMPADD', MINUTE, a.durationMin, a.scheduledAt) > :startTime))")
    List<Appointment> findConflictingAppointments(
            @Param("tenantId") UUID tenantId,
            @Param("professionalId") UUID professionalId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus,
            @Param("excludeId") UUID excludeId
    );
}
