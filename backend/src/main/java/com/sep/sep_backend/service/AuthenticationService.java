package com.sep.sep_backend.service;

import com.sep.sep_backend.dto.AuthResponseDto;
import com.sep.sep_backend.dto.LoginDto;
import com.sep.sep_backend.dto.RegisterDto;
import com.sep.sep_backend.model.User;
import com.sep.sep_backend.repository.UserRepository;
import com.sep.sep_backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public void register(RegisterDto request) {
        // 1. Gibt es den User schon?
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Benutzername bereits vergeben");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("E-Mail bereits vergeben");
        }

        // 2. Hat der User den AGB zugestimmt?
        if (!request.isAgbAccepted()) {
            throw new IllegalArgumentException("Die AGB müssen akzeptiert werden.");
        }

        // 3. User erstellen
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4. AGB akzeptieren und Zeitstempel setzen
        user.setAgbAccepted(true);
        user.setAgbAcceptedAt(LocalDateTime.now()); // Aktuelle Zeit speichern

        // Gamification: Start-Badge vergeben
        user.setBadges(List.of("🐣 Neuankömmling"));

        userRepository.save(user);
    }

    public AuthResponseDto login(LoginDto request) {
        // Spring Security Authentifizierung
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));


        // Token generieren
        String token = jwtUtil.generateToken(request.getUsername());

       boolean introRequired = !user.isIntroExamDone();

        return new AuthResponseDto(token,
                request.getUsername(),
                introRequired,
                user.getIntroExamPercentage(),
                user.getIntroExamLevel());
    }
}