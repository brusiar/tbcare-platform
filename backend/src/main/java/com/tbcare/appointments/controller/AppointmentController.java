package com.tbcare.appointments.controller;

import com.tbcare.appointments.dto.AppointmentRequest;
import com.tbcare.appointments.dto.AppointmentResponse;
import com.tbcare.appointments.service.AppointmentService;
import com.tbcare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.findByPeriod(start, end)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(appointmentService.create(request), "Appointment created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> update(@PathVariable UUID id, @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.update(id, request), "Appointment updated successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.cancel(id), "Appointment cancelled successfully"));
    }
}
