package com.tbcare.professionals.service;

import com.tbcare.professionals.domain.Professional;
import com.tbcare.professionals.dto.ProfessionalRequest;
import com.tbcare.professionals.dto.ProfessionalResponse;
import com.tbcare.professionals.repository.ProfessionalRepository;
import com.tbcare.security.TenantContext;
import com.tbcare.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    public ProfessionalService(ProfessionalRepository professionalRepository,
                              UserRepository userRepository) {
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponse> findAll() {
        UUID tenantId = TenantContext.getTenantId();
        return professionalRepository.findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .map(this::toResponse)
                .sorted((a, b) -> a.getUserName().compareToIgnoreCase(b.getUserName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProfessionalResponse findById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        Professional professional = professionalRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Professional not found"));
        return toResponse(professional);
    }

    @Transactional
    public ProfessionalResponse create(ProfessionalRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        userRepository.findByIdAndTenantId(request.getUserId(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Professional professional = new Professional();
        professional.setTenantId(tenantId);
        professional.setUserId(request.getUserId());
        professional.setMeetLink(request.getMeetLink());
        professional.setSpecialty(request.getSpecialty());
        professional.setActive(true);

        professional = professionalRepository.save(professional);
        return toResponse(professional);
    }

    @Transactional
    public ProfessionalResponse update(UUID id, ProfessionalRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        Professional professional = professionalRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Professional not found"));

        if (request.getUserId() != null) {
            userRepository.findByIdAndTenantId(request.getUserId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            professional.setUserId(request.getUserId());
        }

        if (request.getMeetLink() != null) {
            professional.setMeetLink(request.getMeetLink());
        }

        if (request.getSpecialty() != null) {
            professional.setSpecialty(request.getSpecialty());
        }

        professional = professionalRepository.save(professional);
        return toResponse(professional);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.getTenantId();

        Professional professional = professionalRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Professional not found"));

        professional.setActive(false);
        professionalRepository.save(professional);
    }

    private ProfessionalResponse toResponse(Professional professional) {
        ProfessionalResponse response = new ProfessionalResponse();
        response.setId(professional.getId());
        response.setTenantId(professional.getTenantId());
        response.setUserId(professional.getUserId());
        response.setMeetLink(professional.getMeetLink());
        response.setSpecialty(professional.getSpecialty());
        response.setActive(professional.isActive());
        response.setCreatedAt(professional.getCreatedAt());
        response.setUpdatedAt(professional.getUpdatedAt());

        userRepository.findById(professional.getUserId())
                .ifPresent(user -> {
                    response.setUserName(user.getName());
                    response.setUserEmail(user.getEmail());
                });

        return response;
    }
}
