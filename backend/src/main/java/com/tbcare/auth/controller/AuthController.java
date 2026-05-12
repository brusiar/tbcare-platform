package com.tbcare.auth.controller;

import com.tbcare.auth.dto.LoginRequest;
import com.tbcare.auth.dto.LoginResponse;
import com.tbcare.auth.dto.UserResponse;
import com.tbcare.auth.service.AuthService;
import com.tbcare.common.response.ApiResponse;
import com.tbcare.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse response = new UserResponse(
            userPrincipal.getId(),
            userPrincipal.getName(),
            userPrincipal.getEmail(),
            userPrincipal.getRole().name(),
            userPrincipal.getTenantId()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
