package com.tbcare.professionals.service;

import com.tbcare.professionals.domain.Professional;
import com.tbcare.professionals.dto.ProfessionalRequest;
import com.tbcare.professionals.dto.ProfessionalResponse;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfessionalService professionalService;

    private MockedStatic<TenantContext> tenantContextMock;
    private UUID tenantId;
    private UUID professionalId;
    private UUID userId;
    private Professional professional;
    private User user;
    private ProfessionalRequest professionalRequest;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        professionalId = UUID.randomUUID();
        userId = UUID.randomUUID();

        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        user = new User();
        user.setId(userId);
        user.setName("Dr. John");
        user.setEmail("dr.john@example.com");

        professional = new Professional();
        professional.setId(professionalId);
        professional.setTenantId(tenantId);
        professional.setUserId(userId);
        professional.setMeetLink("https://meet.google.com/abc");
        professional.setSpecialty("Cardiology");
        professional.setActive(true);

        professionalRequest = new ProfessionalRequest();
        professionalRequest.setUserId(userId);
        professionalRequest.setMeetLink("https://meet.google.com/abc");
        professionalRequest.setSpecialty("Cardiology");
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void findAll_ShouldReturnListOfActiveProfessionals() {
        // Arrange
        List<Professional> professionals = Arrays.asList(professional);
        when(professionalRepository.findByTenantIdAndActiveTrue(tenantId)).thenReturn(professionals);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        List<ProfessionalResponse> result = professionalService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. John", result.get(0).getUserName());
        assertEquals("Cardiology", result.get(0).getSpecialty());
        verify(professionalRepository).findByTenantIdAndActiveTrue(tenantId);
    }

    @Test
    void findById_WithValidId_ShouldReturnProfessional() {
        // Arrange
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.of(professional));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ProfessionalResponse result = professionalService.findById(professionalId);

        // Assert
        assertNotNull(result);
        assertEquals(professionalId, result.getId());
        assertEquals("Dr. John", result.getUserName());
        assertEquals("Cardiology", result.getSpecialty());
        verify(professionalRepository).findByIdAndTenantId(professionalId, tenantId);
    }

    @Test
    void findById_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(professionalRepository.findByIdAndTenantId(invalidId, tenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> professionalService.findById(invalidId));
        verify(professionalRepository).findByIdAndTenantId(invalidId, tenantId);
    }

    @Test
    void create_WithValidRequest_ShouldReturnCreatedProfessional() {
        // Arrange
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ProfessionalResponse result = professionalService.create(professionalRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Cardiology", result.getSpecialty());
        assertEquals(tenantId, result.getTenantId());
        assertTrue(result.isActive());
        verify(userRepository).findByIdAndTenantId(userId, tenantId);
        verify(professionalRepository).save(any(Professional.class));
    }

    @Test
    void create_WithInvalidUserId_ShouldThrowEntityNotFoundException() {
        // Arrange
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> professionalService.create(professionalRequest));
        verify(userRepository).findByIdAndTenantId(userId, tenantId);
        verify(professionalRepository, never()).save(any());
    }

    @Test
    void update_WithValidRequest_ShouldReturnUpdatedProfessional() {
        // Arrange
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.of(professional));
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        professionalRequest.setSpecialty("Neurology");

        // Act
        ProfessionalResponse result = professionalService.update(professionalId, professionalRequest);

        // Assert
        assertNotNull(result);
        verify(professionalRepository).findByIdAndTenantId(professionalId, tenantId);
        verify(professionalRepository).save(any(Professional.class));
    }

    @Test
    void delete_WithValidId_ShouldDeactivateProfessional() {
        // Arrange
        when(professionalRepository.findByIdAndTenantId(professionalId, tenantId))
            .thenReturn(Optional.of(professional));
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);

        // Act
        professionalService.delete(professionalId);

        // Assert
        verify(professionalRepository).findByIdAndTenantId(professionalId, tenantId);
        verify(professionalRepository).save(argThat(p -> !p.isActive()));
    }
}
