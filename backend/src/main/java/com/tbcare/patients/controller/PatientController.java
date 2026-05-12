package com.tbcare.patients.controller;

import com.tbcare.common.response.ApiResponse;
import com.tbcare.patients.domain.Patient;
import com.tbcare.patients.service.PatientService;
import com.tbcare.security.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Patient>>> findAll() {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(ApiResponse.ok(patientService.findAll(tenantId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Patient>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Patient>> create(@RequestBody Patient patient) {
        UUID tenantId = TenantContext.getTenantId();
        patient.setTenantId(tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(patientService.create(patient), "Patient created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Patient>> update(@PathVariable UUID id, @RequestBody Patient patient) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.update(id, patient)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        patientService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
