package com.tbcare.patients.controller;

import com.tbcare.common.response.ApiResponse;
import com.tbcare.patients.domain.Patient;
import com.tbcare.patients.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
public class PatientController {

    // TODO: Replace with tenant from security context when auth is implemented
    private static final UUID DEFAULT_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Patient>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(patientService.findAll(DEFAULT_TENANT)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Patient>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Patient>> create(@RequestBody Patient patient) {
        patient.setTenantId(DEFAULT_TENANT);
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
