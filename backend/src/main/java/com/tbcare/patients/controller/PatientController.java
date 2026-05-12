package com.tbcare.patients.controller;

import com.tbcare.common.response.ApiResponse;
import com.tbcare.patients.dto.PatientRequest;
import com.tbcare.patients.dto.PatientResponse;
import com.tbcare.patients.service.PatientService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<List<PatientResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(patientService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> create(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(patientService.create(request), "Patient created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> update(@PathVariable UUID id, @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.update(id, request), "Patient updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        patientService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Patient deactivated successfully"));
    }
}
