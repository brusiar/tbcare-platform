package com.tbcare.auth.dto;

import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String role;
    private UUID tenantId;

    public UserResponse(UUID id, String name, String email, String role, UUID tenantId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.tenantId = tenantId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
}
