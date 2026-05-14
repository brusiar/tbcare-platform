package com.tbcare.appointments.controller;

import com.tbcare.appointments.dto.RecurrenceRequest;
import com.tbcare.appointments.dto.RecurrenceResponse;
import com.tbcare.appointments.service.RecurrenceService;
import com.tbcare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurrences")
public class RecurrenceController {

    private final RecurrenceService recurrenceService;

    public RecurrenceController(RecurrenceService recurrenceService) {
        this.recurrenceService = recurrenceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurrenceResponse>> createRecurrence(
            @Valid @RequestBody RecurrenceRequest request) {
        RecurrenceResponse response = recurrenceService.createRecurrence(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
