package com.tbcare.appointments.service;

import com.tbcare.appointments.domain.Appointment;
import com.tbcare.appointments.domain.AppointmentRecurrence;
import com.tbcare.appointments.domain.AppointmentStatus;
import com.tbcare.appointments.domain.RecurrenceType;
import com.tbcare.appointments.dto.RecurrenceRequest;
import com.tbcare.appointments.dto.RecurrenceResponse;
import com.tbcare.appointments.repository.AppointmentRecurrenceRepository;
import com.tbcare.appointments.repository.AppointmentRepository;
import com.tbcare.patients.repository.PatientRepository;
import com.tbcare.professionals.repository.ProfessionalRepository;
import com.tbcare.security.TenantContext;
import com.tbcare.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecurrenceService {

    private final AppointmentRecurrenceRepository recurrenceRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    public RecurrenceService(AppointmentRecurrenceRepository recurrenceRepository,
                            AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            ProfessionalRepository professionalRepository,
                            UserRepository userRepository) {
        this.recurrenceRepository = recurrenceRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RecurrenceResponse createRecurrence(RecurrenceRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        // Validate patient and professional
        patientRepository.findByIdAndTenantId(request.getPatientId(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
        
        var professional = professionalRepository.findByIdAndTenantId(request.getProfessionalId(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Professional not found"));

        // Create recurrence
        AppointmentRecurrence recurrence = new AppointmentRecurrence();
        recurrence.setTenantId(tenantId);
        recurrence.setPatientId(request.getPatientId());
        recurrence.setProfessionalId(request.getProfessionalId());
        recurrence.setRecurrenceType(request.getRecurrenceType());
        recurrence.setStartDate(request.getStartDate());
        recurrence.setEndDate(request.getEndDate());
        recurrence.setDurationMin(request.getDurationMin());
        recurrence.setTimeOfDay(request.getTimeOfDay());
        recurrence.setDayOfWeek(request.getDayOfWeek());
        recurrence.setActive(true);

        recurrence = recurrenceRepository.save(recurrence);

        // Generate appointments
        int count = generateAppointments(recurrence, professional.getMeetLink(), request.getNotes());

        return toResponse(recurrence, count);
    }

    private int generateAppointments(AppointmentRecurrence recurrence, String meetLink, String notes) {
        List<LocalDate> dates = calculateDates(recurrence);
        LocalTime time = LocalTime.parse(recurrence.getTimeOfDay());
        int count = 0;

        for (LocalDate date : dates) {
            LocalDateTime scheduledAt = LocalDateTime.of(date, time);

            // Check for conflicts
            LocalDateTime endTime = scheduledAt.plusMinutes(recurrence.getDurationMin());
            List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                recurrence.getTenantId(),
                recurrence.getProfessionalId(),
                scheduledAt,
                endTime,
                AppointmentStatus.CANCELLED,
                UUID.randomUUID()
            );

            if (conflicts.isEmpty()) {
                Appointment appointment = new Appointment();
                appointment.setTenantId(recurrence.getTenantId());
                appointment.setPatientId(recurrence.getPatientId());
                appointment.setProfessionalId(recurrence.getProfessionalId());
                appointment.setRecurrenceId(recurrence.getId());
                appointment.setScheduledAt(scheduledAt);
                appointment.setDurationMin(recurrence.getDurationMin());
                appointment.setStatus(AppointmentStatus.SCHEDULED);
                appointment.setMeetLink(meetLink);
                appointment.setNotes(notes);

                appointmentRepository.save(appointment);
                count++;
            }
        }

        return count;
    }

    private List<LocalDate> calculateDates(AppointmentRecurrence recurrence) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = recurrence.getStartDate();
        LocalDate end = recurrence.getEndDate() != null ? recurrence.getEndDate() : current.plusMonths(3);

        while (!current.isAfter(end)) {
            if (matchesRecurrence(current, recurrence)) {
                dates.add(current);
            }
            current = current.plusDays(1);
        }

        return dates;
    }

    private boolean matchesRecurrence(LocalDate date, AppointmentRecurrence recurrence) {
        return switch (recurrence.getRecurrenceType()) {
            case DAILY -> true;
            case WEEKLY -> recurrence.getDayOfWeek() != null && 
                          date.getDayOfWeek().getValue() == recurrence.getDayOfWeek();
            case BIWEEKLY -> {
                if (recurrence.getDayOfWeek() == null) yield false;
                long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(recurrence.getStartDate(), date);
                yield date.getDayOfWeek().getValue() == recurrence.getDayOfWeek() && weeksBetween % 2 == 0;
            }
            case MONTHLY -> date.getDayOfMonth() == recurrence.getStartDate().getDayOfMonth();
            case NONE -> false;
        };
    }

    private RecurrenceResponse toResponse(AppointmentRecurrence recurrence, int count) {
        RecurrenceResponse response = new RecurrenceResponse();
        response.setId(recurrence.getId());
        response.setTenantId(recurrence.getTenantId());
        response.setPatientId(recurrence.getPatientId());
        response.setProfessionalId(recurrence.getProfessionalId());
        response.setRecurrenceType(recurrence.getRecurrenceType());
        response.setStartDate(recurrence.getStartDate());
        response.setEndDate(recurrence.getEndDate());
        response.setDurationMin(recurrence.getDurationMin());
        response.setTimeOfDay(recurrence.getTimeOfDay());
        response.setDayOfWeek(recurrence.getDayOfWeek());
        response.setActive(recurrence.isActive());
        response.setGeneratedAppointmentsCount(count);
        response.setCreatedAt(recurrence.getCreatedAt());
        response.setUpdatedAt(recurrence.getUpdatedAt());

        // Load names
        patientRepository.findById(recurrence.getPatientId())
                .ifPresent(p -> response.setPatientName(p.getName()));
        
        professionalRepository.findById(recurrence.getProfessionalId())
                .ifPresent(prof -> userRepository.findById(prof.getUserId())
                        .ifPresent(u -> response.setProfessionalName(u.getName())));

        return response;
    }
}
