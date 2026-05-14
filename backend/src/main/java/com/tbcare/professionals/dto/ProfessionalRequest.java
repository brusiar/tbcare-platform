package com.tbcare.professionals.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ProfessionalRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String meetLink;
    private String specialty;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
