package com.tbcare.appointments.service;

import com.tbcare.appointments.domain.Appointment;
import com.tbcare.appointments.domain.AppointmentStatus;
import com.tbcare.appointments.dto.AppointmentRequest;
import com.tbcare.appointments.dto.AppointmentResponse;
import com.tbcare.appointments.repository.AppointmentRepository;
import com.tbcare.patients.repository.PatientRepository;
import com.tbcare.professionals.repository.ProfessionalRepository;
import com.tbcare.security.TenantContext;
import com.tbcare.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            ProfessionalRepository professionalRepository,
                            UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByPeriod(LocalDateTime start, LocalDateTime end) {
        UUID tenantId = TenantContext.getTenantId();
        return appointmentRepository.findByTenantIdAndScheduledAtBetween(tenantId, start, end)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        // Validate patient exists
        patientRepository.findByIdAndTenantId(request.getPatientId(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        // Validate professional exists
        var professional = professionalRepository.findByIdAndTenantId(request.getProfessionalId(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Professional not found"));

        Appointment appointment = new Appointment();
        appointment.setTenantId(tenantId);
        appointment.setPatientId(request.getPatientId());
        appointment.setProfessionalId(request.getProfessionalId());
        appointment.setScheduledAt(request.getScheduledAt());
        appointment.setDurationMin(request.getDurationMin() != null ? request.getDurationMin() : 60);
        appointment.setStatus(request.getStatus() != null ? request.getStatus() : AppointmentStatus.SCHEDULED);
        appointment.setMeetLink(request.getMeetLink() != null ? request.getMeetLink() : professional.getMeetLink());
        appointment.setNotes(request.getNotes());
        appointment.setRecurrenceId(request.getRecurrenceId());

        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse update(UUID id, AppointmentRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        if (request.getPatientId() != null) {
            patientRepository.findByIdAndTenantId(request.getPatientId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
            appointment.setPatientId(request.getPatientId());
        }

        if (request.getProfessionalId() != null) {
            professionalRepository.findByIdAndTenantId(request.getProfessionalId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Professional not found"));
            appointment.setProfessionalId(request.getProfessionalId());
        }

        if (request.getScheduledAt() != null) {
            appointment.setScheduledAt(request.getScheduledAt());
        }

        if (request.getDurationMin() != null) {
            appointment.setDurationMin(request.getDurationMin());
        }

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        if (request.getMeetLink() != null) {
            appointment.setMeetLink(request.getMeetLink());
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(UUID id) {
        UUID tenantId = TenantContext.getTenantId();

        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatientId());
        response.setProfessionalId(appointment.getProfessionalId());
        response.setRecurrenceId(appointment.getRecurrenceId());
        response.setScheduledAt(appointment.getScheduledAt());
        response.setDurationMin(appointment.getDurationMin());
        response.setStatus(appointment.getStatus());
        response.setMeetLink(appointment.getMeetLink());
        response.setNotes(appointment.getNotes());
        response.setTenantId(appointment.getTenantId());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());

        // Load patient name
        patientRepository.findById(appointment.getPatientId())
                .ifPresent(patient -> response.setPatientName(patient.getName()));

        // Load professional name
        professionalRepository.findById(appointment.getProfessionalId())
                .ifPresent(prof -> userRepository.findById(prof.getUserId())
                        .ifPresent(user -> response.setProfessionalName(user.getName())));

        return response;
    }
}
