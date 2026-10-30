package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.AuthResponseDto;
import com.sep.sep_backend.dto.LoginDto;
import com.sep.sep_backend.dto.RegisterDto;
import com.sep.sep_backend.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(Map.of("message", "Registrierung erfolgreich"));
        } catch (IllegalArgumentException e) {
            // Fängt Fehler wie "Email schon vergeben" ab
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}