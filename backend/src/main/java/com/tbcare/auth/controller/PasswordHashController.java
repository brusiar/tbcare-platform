package com.tbcare.auth.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hash")
public class PasswordHashController {

    private final PasswordEncoder passwordEncoder;

    public PasswordHashController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{password}")
    public String generateHash(@PathVariable String password) {
        return passwordEncoder.encode(password);
    }
}
