package com.tbcare.appointments.dto;

import com.tbcare.appointments.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Professional ID is required")
    private UUID professionalId;

    @NotNull(message = "Scheduled date/time is required")
    private LocalDateTime scheduledAt;

    private Integer durationMin = 60;

    private AppointmentStatus status;

    private String meetLink;

    private String notes;

    private UUID recurrenceId;

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(UUID professionalId) {
        this.professionalId = professionalId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Integer getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(Integer durationMin) {
        this.durationMin = durationMin;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UUID getRecurrenceId() {
        return recurrenceId;
    }

    public void setRecurrenceId(UUID recurrenceId) {
        this.recurrenceId = recurrenceId;
    }
}
