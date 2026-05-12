package com.tbcare.patients.service;

import com.tbcare.patients.domain.Patient;
import com.tbcare.patients.dto.PatientRequest;
import com.tbcare.patients.dto.PatientResponse;
import com.tbcare.patients.repository.PatientRepository;
import com.tbcare.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> findAll() {
        UUID tenantId = TenantContext.getTenantId();
        return patientRepository.findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        Patient patient = patientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
        return toResponse(patient);
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        
        Patient patient = new Patient();
        patient.setTenantId(tenantId);
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setNotes(request.getNotes());
        patient.setActive(true);

        patient = patientRepository.save(patient);
        return toResponse(patient);
    }

    @Transactional
    public PatientResponse update(UUID id, PatientRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        
        Patient patient = patientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setNotes(request.getNotes());

        patient = patientRepository.save(patient);
        return toResponse(patient);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        
        Patient patient = patientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        patient.setActive(false);
        patientRepository.save(patient);
    }

    private PatientResponse toResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setName(patient.getName());
        response.setEmail(patient.getEmail());
        response.setPhone(patient.getPhone());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setNotes(patient.getNotes());
        response.setActive(patient.isActive());
        response.setTenantId(patient.getTenantId());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());
        return response;
    }
}
