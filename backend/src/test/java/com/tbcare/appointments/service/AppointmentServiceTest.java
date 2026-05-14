package com.tbcare.appointments.service;

import com.tbcare.appointments.domain.Appointment;
import com.tbcare.appointments.domain.AppointmentStatus;
import com.tbcare.appointments.dto.AppointmentRequest;
import com.tbcare.appointments.dto.AppointmentResponse;
import com.tbcare.appointments.repository.AppointmentRepository;
import com.tbcare.common.exception.AppointmentConflictException;
import com.tbcare.patients.domain.Patient;
import com.tbcare.patients.repository.PatientRepository;
import com.tbcare.professionals.domain.Professional;
import com.tbcare.professionals.repository.ProfessionalRepository;
import com.tbcare.security.TenantContext;
import com.tbcare.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private MockedStatic<TenantContext> tenantContextMock;
    private UUID tenantId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID professionalId;
    private Appointment appointment;
    private Patient patient;
    private Professional professional;
    private AppointmentRequest appointmentRequest;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        professionalId = UUID.randomUUID();

        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        patient = new Patient();
        patient.setId(patientId);
        patient.setName("John Doe");

        professional = new Professional();
        professional.setId(professionalId);
        professional.setMeetLink("https://meet.google.com/abc");

        appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setTenantId(tenantId);
        appointment.setPatientId(patientId);
        appointment.setProfessionalId(professionalId);
        appointment.setScheduledAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        appointment.setDurationMin(60);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setMeetLink("https://meet.google.com/abc");

        appointmentRequest = new AppointmentRequest();
        appointmentRequest.setPatientId(patientId);
        appointmentRequest.setProfessionalId(professionalId);
        appointmentRequest.setScheduledAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        appointmentRequest.setDurationMin(60);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void create_WithValidRequest_ShouldReturnCreatedAppointment() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.of(professional));
        when(appointmentRepository.findConflictingAppointments(
            any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        // Act
        AppointmentResponse result = appointmentService.create(appointmentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(appointmentId, result.getId());
        assertEquals(patientId, result.getPatientId());
        assertEquals(professionalId, result.getProfessionalId());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void create_WithConflictingAppointment_ShouldThrowAppointmentConflictException() {
        // Arrange
        Appointment conflictingAppointment = new Appointment();
        conflictingAppointment.setId(UUID.randomUUID());

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.of(professional));
        when(appointmentRepository.findConflictingAppointments(
            any(), any(), any(), any(), any(), any()))
            .thenReturn(Arrays.asList(conflictingAppointment));

        // Act & Assert
        assertThrows(AppointmentConflictException.class, 
            () -> appointmentService.create(appointmentRequest));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_WithInvalidPatient_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> appointmentService.create(appointmentRequest));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_WithInvalidProfessional_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> appointmentService.create(appointmentRequest));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void findByPeriod_ShouldReturnAppointmentsInPeriod() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 15, 23, 59);
        when(appointmentRepository.findByTenantIdAndScheduledAtBetween(tenantId, start, end))
            .thenReturn(Arrays.asList(appointment));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        // Act
        var result = appointmentService.findByPeriod(start, end);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findByTenantIdAndScheduledAtBetween(tenantId, start, end);
    }

    @Test
    void cancel_WithValidId_ShouldCancelAppointment() {
        // Arrange
        when(appointmentRepository.findByIdAndTenantId(appointmentId, tenantId))
            .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        // Act
        AppointmentResponse result = appointmentService.cancel(appointmentId);

        // Assert
        assertNotNull(result);
        verify(appointmentRepository).save(argThat(a -> 
            a.getStatus() == AppointmentStatus.CANCELLED));
    }

    @Test
    void update_WithTimeChange_ShouldValidateConflicts() {
        // Arrange
        when(appointmentRepository.findByIdAndTenantId(appointmentId, tenantId))
            .thenReturn(Optional.of(appointment));
        when(appointmentRepository.findConflictingAppointments(
            any(), any(), any(), any(), any(), eq(appointmentId)))
            .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        appointmentRequest.setScheduledAt(LocalDateTime.of(2024, 1, 15, 14, 0));

        // Act
        AppointmentResponse result = appointmentService.update(appointmentId, appointmentRequest);

        // Assert
        assertNotNull(result);
        verify(appointmentRepository).findConflictingAppointments(
            any(), any(), any(), any(), any(), eq(appointmentId));
        verify(appointmentRepository).save(any(Appointment.class));
    }
}
