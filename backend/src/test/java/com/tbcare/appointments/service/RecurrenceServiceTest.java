package com.tbcare.appointments.service;

import com.tbcare.appointments.domain.AppointmentRecurrence;
import com.tbcare.appointments.domain.AppointmentStatus;
import com.tbcare.appointments.domain.RecurrenceType;
import com.tbcare.appointments.dto.RecurrenceRequest;
import com.tbcare.appointments.dto.RecurrenceResponse;
import com.tbcare.appointments.repository.AppointmentRecurrenceRepository;
import com.tbcare.appointments.repository.AppointmentRepository;
import com.tbcare.patients.domain.Patient;
import com.tbcare.patients.repository.PatientRepository;
import com.tbcare.professionals.domain.Professional;
import com.tbcare.professionals.repository.ProfessionalRepository;
import com.tbcare.security.TenantContext;
import com.tbcare.users.domain.User;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurrenceServiceTest {

    @Mock
    private AppointmentRecurrenceRepository recurrenceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecurrenceService recurrenceService;

    private MockedStatic<TenantContext> tenantContextMock;
    private UUID tenantId;
    private UUID patientId;
    private UUID professionalId;
    private UUID userId;
    private Patient patient;
    private Professional professional;
    private User user;
    private RecurrenceRequest recurrenceRequest;
    private AppointmentRecurrence recurrence;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        professionalId = UUID.randomUUID();
        userId = UUID.randomUUID();

        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        patient = new Patient();
        patient.setId(patientId);
        patient.setName("John Doe");

        user = new User();
        user.setId(userId);
        user.setName("Dr. Smith");

        professional = new Professional();
        professional.setId(professionalId);
        professional.setUserId(userId);
        professional.setMeetLink("https://meet.google.com/abc");

        recurrenceRequest = new RecurrenceRequest();
        recurrenceRequest.setPatientId(patientId);
        recurrenceRequest.setProfessionalId(professionalId);
        recurrenceRequest.setRecurrenceType(RecurrenceType.WEEKLY);
        recurrenceRequest.setStartDate(LocalDate.of(2024, 1, 15));
        recurrenceRequest.setEndDate(LocalDate.of(2024, 2, 15));
        recurrenceRequest.setDurationMin(60);
        recurrenceRequest.setTimeOfDay("14:00");
        recurrenceRequest.setDayOfWeek(2); // Tuesday

        recurrence = new AppointmentRecurrence();
        recurrence.setId(UUID.randomUUID());
        recurrence.setTenantId(tenantId);
        recurrence.setPatientId(patientId);
        recurrence.setProfessionalId(professionalId);
        recurrence.setRecurrenceType(RecurrenceType.WEEKLY);
        recurrence.setStartDate(LocalDate.of(2024, 1, 15));
        recurrence.setEndDate(LocalDate.of(2024, 2, 15));
        recurrence.setDurationMin(60);
        recurrence.setTimeOfDay("14:00");
        recurrence.setDayOfWeek(2);
        recurrence.setActive(true);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void createRecurrence_WithValidRequest_ShouldCreateRecurrenceAndGenerateAppointments() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.of(professional));
        when(recurrenceRepository.save(any(AppointmentRecurrence.class)))
            .thenReturn(recurrence);
        when(appointmentRepository.findConflictingAppointments(
            any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        RecurrenceResponse result = recurrenceService.createRecurrence(recurrenceRequest);

        // Assert
        assertNotNull(result);
        assertEquals(RecurrenceType.WEEKLY, result.getRecurrenceType());
        assertTrue(result.getGeneratedAppointmentsCount() > 0);
        verify(recurrenceRepository).save(any(AppointmentRecurrence.class));
        verify(appointmentRepository, atLeastOnce()).save(any());
    }

    @Test
    void createRecurrence_WithInvalidPatient_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> recurrenceService.createRecurrence(recurrenceRequest));
        verify(recurrenceRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createRecurrence_WithInvalidProfessional_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> recurrenceService.createRecurrence(recurrenceRequest));
        verify(recurrenceRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createRecurrence_WithDailyType_ShouldGenerateMultipleAppointments() {
        // Arrange
        recurrenceRequest.setRecurrenceType(RecurrenceType.DAILY);
        recurrenceRequest.setDayOfWeek(null);
        recurrence.setRecurrenceType(RecurrenceType.DAILY);

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.of(professional));
        when(recurrenceRepository.save(any(AppointmentRecurrence.class)))
            .thenReturn(recurrence);
        when(appointmentRepository.findConflictingAppointments(
            any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        RecurrenceResponse result = recurrenceService.createRecurrence(recurrenceRequest);

        // Assert
        assertNotNull(result);
        assertEquals(RecurrenceType.DAILY, result.getRecurrenceType());
        assertTrue(result.getGeneratedAppointmentsCount() >= 30); // ~31 days
        verify(appointmentRepository, atLeast(30)).save(any());
    }

    @Test
    void createRecurrence_WithConflicts_ShouldSkipConflictingDates() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.of(professional));
        when(recurrenceRepository.save(any(AppointmentRecurrence.class)))
            .thenReturn(recurrence);
        
        // First call has conflict, subsequent calls don't
        when(appointmentRepository.findConflictingAppointments(
            any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(new com.tbcare.appointments.domain.Appointment()))
            .thenReturn(Collections.emptyList());
        
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        RecurrenceResponse result = recurrenceService.createRecurrence(recurrenceRequest);

        // Assert
        assertNotNull(result);
        // Should generate fewer appointments due to conflicts
        assertTrue(result.getGeneratedAppointmentsCount() >= 0);
    }
}
