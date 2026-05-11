package com.tbcare.appointments.controller;

import com.tbcare.appointments.domain.Appointment;
import com.tbcare.appointments.service.AppointmentService;
import com.tbcare.common.response.ApiResponse;
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

    // TODO: Replace with tenant from security context when auth is implemented
    private static final UUID DEFAULT_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Appointment>>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.findByPeriod(DEFAULT_TENANT, start, end)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Appointment>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Appointment>> create(@RequestBody Appointment appointment) {
        appointment.setTenantId(DEFAULT_TENANT);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(appointmentService.create(appointment), "Appointment created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Appointment>> update(@PathVariable UUID id, @RequestBody Appointment appointment) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.update(id, appointment)));
    }
}
