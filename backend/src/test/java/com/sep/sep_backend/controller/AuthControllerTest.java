package com.sep.sep_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.sep_backend.dto.AuthResponseDto;
import com.sep.sep_backend.dto.LoginDto;
import com.sep.sep_backend.dto.RegisterDto;
import com.sep.sep_backend.security.JwtUtil;
import com.sep.sep_backend.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// Spring Boot 3.4+ nutzt @MockitoBean.
// Bei älteren Versionen nutze @MockBean
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private com.sep.sep_backend.service.CustomUserDetailsService customUserDetailsService;

    // --- REGISTER TESTS ---

    @Test
    void register_Success() throws Exception {
        // Arrange
        RegisterDto registerDto = new RegisterDto("testuser", "test@example.com", "password123");

        // Act & Assert
        // Da der Service void zurückgibt, müssen wir nichts mocken (default ist doNothing)

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf()) // Wichtig für Security Tests
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registrierung erfolgreich"));
    }

    @Test
    void register_Fail_UsernameOrEmailTaken() throws Exception {
        // Arrange
        RegisterDto registerDto = new RegisterDto("existingUser", "test@example.com", "password123");

        // Simuliere, dass der Service einen Fehler wirft (z.B. User existiert schon)
        doThrow(new IllegalArgumentException("Benutzername bereits vergeben"))
                .when(authService).register(any(RegisterDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Benutzername bereits vergeben"));
    }

    @Test
    void register_Fail_InvalidInput() throws Exception {
        // Arrange: Leeres Passwort/User (löst @Valid Fehler aus)
        RegisterDto invalidDto = new RegisterDto("", "invalid-email", "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
        // Spring Boot gibt standardmäßig 400 zurück, wenn @Valid fehlschlägt
    }

    // --- LOGIN TESTS ---

    @Test
    void login_Success() throws Exception {
        // Arrange
        LoginDto loginDto = new LoginDto("testuser", "password123");
        AuthResponseDto responseDto = new AuthResponseDto("fake-jwt-token", "testuser");

        when(authService.login(any(LoginDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }}