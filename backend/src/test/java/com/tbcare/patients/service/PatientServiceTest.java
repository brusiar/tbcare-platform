package com.tbcare.patients.service;

import com.tbcare.patients.domain.Patient;
import com.tbcare.patients.dto.PatientRequest;
import com.tbcare.patients.dto.PatientResponse;
import com.tbcare.patients.repository.PatientRepository;
import com.tbcare.security.TenantContext;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private MockedStatic<TenantContext> tenantContextMock;
    private UUID tenantId;
    private UUID patientId;
    private Patient patient;
    private PatientRequest patientRequest;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        patient = new Patient();
        patient.setId(patientId);
        patient.setTenantId(tenantId);
        patient.setName("John Doe");
        patient.setEmail("john@example.com");
        patient.setPhone("123456789");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setNotes("Test notes");
        patient.setActive(true);

        patientRequest = new PatientRequest();
        patientRequest.setName("John Doe");
        patientRequest.setEmail("john@example.com");
        patientRequest.setPhone("123456789");
        patientRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientRequest.setNotes("Test notes");
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void findAll_ShouldReturnListOfActivePatients() {
        // Arrange
        List<Patient> patients = Arrays.asList(patient);
        when(patientRepository.findByTenantIdAndActiveTrue(tenantId)).thenReturn(patients);

        // Act
        List<PatientResponse> result = patientService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("john@example.com", result.get(0).getEmail());
        verify(patientRepository).findByTenantIdAndActiveTrue(tenantId);
    }

    @Test
    void findById_WithValidId_ShouldReturnPatient() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));

        // Act
        PatientResponse result = patientService.findById(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(patientId, result.getId());
        assertEquals("John Doe", result.getName());
        verify(patientRepository).findByIdAndTenantId(patientId, tenantId);
    }

    @Test
    void findById_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(patientRepository.findByIdAndTenantId(invalidId, tenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> patientService.findById(invalidId));
        verify(patientRepository).findByIdAndTenantId(invalidId, tenantId);
    }

    @Test
    void create_WithValidRequest_ShouldReturnCreatedPatient() {
        // Arrange
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        // Act
        PatientResponse result = patientService.create(patientRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(tenantId, result.getTenantId());
        assertTrue(result.isActive());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void update_WithValidRequest_ShouldReturnUpdatedPatient() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        patientRequest.setName("Jane Doe");
        patientRequest.setEmail("jane@example.com");

        // Act
        PatientResponse result = patientService.update(patientId, patientRequest);

        // Assert
        assertNotNull(result);
        verify(patientRepository).findByIdAndTenantId(patientId, tenantId);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void update_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(patientRepository.findByIdAndTenantId(invalidId, tenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> patientService.update(invalidId, patientRequest));
        verify(patientRepository).findByIdAndTenantId(invalidId, tenantId);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void delete_WithValidId_ShouldDeactivatePatient() {
        // Arrange
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
            .thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        // Act
        patientService.delete(patientId);

        // Assert
        verify(patientRepository).findByIdAndTenantId(patientId, tenantId);
        verify(patientRepository).save(argThat(p -> !p.isActive()));
    }

    @Test
    void delete_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(patientRepository.findByIdAndTenantId(invalidId, tenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> patientService.delete(invalidId));
        verify(patientRepository).findByIdAndTenantId(invalidId, tenantId);
        verify(patientRepository, never()).save(any());
    }
}
