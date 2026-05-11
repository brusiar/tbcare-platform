package com.tbcare.patients.service;

import com.tbcare.patients.domain.Patient;
import com.tbcare.patients.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> findAll(UUID tenantId) {
        return patientRepository.findByTenantIdAndActiveTrue(tenantId);
    }

    public Patient findById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + id));
    }

    @Transactional
    public Patient create(Patient patient) {
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient update(UUID id, Patient updated) {
        Patient existing = findById(id);
        existing.setName(updated.getName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setNotes(updated.getNotes());
        return patientRepository.save(existing);
    }

    @Transactional
    public void deactivate(UUID id) {
        Patient patient = findById(id);
        patient.setActive(false);
        patientRepository.save(patient);
    }
}
