package com.tbcare.dashboard.service;

import com.tbcare.appointments.domain.AppointmentStatus;
import com.tbcare.appointments.repository.AppointmentRepository;
import com.tbcare.common.dto.DashboardStats;
import com.tbcare.patients.repository.PatientRepository;
import com.tbcare.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class DashboardService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public DashboardService(PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        UUID tenantId = TenantContext.getTenantId();
        DashboardStats stats = new DashboardStats();

        // Active patients count
        stats.setActivePatientsCount(
            patientRepository.findByTenantIdAndActiveTrue(tenantId).size()
        );

        // Today's appointments
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        stats.setAppointmentsTodayCount(
            appointmentRepository.findByTenantIdAndScheduledAtBetween(tenantId, startOfDay, endOfDay).size()
        );

        // This week's appointments
        LocalDateTime startOfWeek = LocalDateTime.now().with(DayOfWeek.MONDAY).with(LocalTime.MIN);
        LocalDateTime endOfWeek = LocalDateTime.now().with(DayOfWeek.SUNDAY).with(LocalTime.MAX);
        stats.setAppointmentsThisWeekCount(
            appointmentRepository.findByTenantIdAndScheduledAtBetween(tenantId, startOfWeek, endOfWeek).size()
        );

        // Pending appointments (SCHEDULED status, future dates)
        LocalDateTime now = LocalDateTime.now();
        long pendingCount = appointmentRepository.findByTenantIdAndScheduledAtBetween(
            tenantId, now, now.plusMonths(1)
        ).stream()
        .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
        .count();
        stats.setPendingAppointmentsCount(pendingCount);

        return stats;
    }
}
