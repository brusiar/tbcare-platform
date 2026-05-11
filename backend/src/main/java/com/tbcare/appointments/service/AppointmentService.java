package com.tbcare.appointments.service;

import com.tbcare.appointments.domain.Appointment;
import com.tbcare.appointments.repository.AppointmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> findByPeriod(UUID tenantId, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByTenantIdAndScheduledAtBetween(tenantId, start, end);
    }

    public Appointment findById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found: " + id));
    }

    @Transactional
    public Appointment create(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment update(UUID id, Appointment updated) {
        Appointment existing = findById(id);
        existing.setScheduledAt(updated.getScheduledAt());
        existing.setDurationMin(updated.getDurationMin());
        existing.setStatus(updated.getStatus());
        existing.setNotes(updated.getNotes());
        return appointmentRepository.save(existing);
    }
}
