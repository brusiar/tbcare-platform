package com.tbcare.professionals.domain;

import com.tbcare.common.TenantAwareEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "professionals")
public class Professional extends TenantAwareEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "meet_link")
    private String meetLink;

    @Column(name = "specialty")
    private String specialty;

    @Column(nullable = false)
    private boolean active = true;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
