package com.tbcare.professionals.controller;

import com.tbcare.common.response.ApiResponse;
import com.tbcare.professionals.dto.ProfessionalRequest;
import com.tbcare.professionals.dto.ProfessionalResponse;
import com.tbcare.professionals.service.ProfessionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProfessionalResponse>>> findAll() {
        List<ProfessionalResponse> professionals = professionalService.findAll();
        return ResponseEntity.ok(ApiResponse.success(professionals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfessionalResponse>> findById(@PathVariable UUID id) {
        ProfessionalResponse professional = professionalService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(professional));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProfessionalResponse>> create(@Valid @RequestBody ProfessionalRequest request) {
        ProfessionalResponse professional = professionalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(professional));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfessionalResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProfessionalRequest request) {
        ProfessionalResponse professional = professionalService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(professional));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        professionalService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
