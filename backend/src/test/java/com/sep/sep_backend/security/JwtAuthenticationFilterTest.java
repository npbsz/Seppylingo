package com.sep.sep_backend.security;

import com.sep.sep_backend.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // Sicherstellen, dass der SecurityContext vor jedem Test leer ist
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validToken_authenticatesUser() throws ServletException, IOException {
        String token = "valid.token";
        String username = "testUser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        request.addHeader("Authorization", "Bearer " + token);

        // Mocks konfigurieren
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        // Ausführen
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Prüfen
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Benutzer sollte authentifiziert sein");
        verify(filterChain).doFilter(request, response); // Request muss weitergeleitet werden
    }

    @Test
    void doFilterInternal_noHeader_continuesChainWithoutAuth() throws ServletException, IOException {
        // Kein Authorization Header gesetzt

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil); // Sollte gar nicht erst aufgerufen werden
    }

    @Test
    void doFilterInternal_expiredToken_logsInfoAndContinuesWithoutAuth() throws ServletException, IOException {
        String token = "expired.token";
        request.addHeader("Authorization", "Bearer " + token);

        // Mock wirft ExpiredJwtException
        // Wir müssen hier mock(ExpiredJwtException.class) verwenden oder eine Instanz, da der Konstruktor komplex ist
        when(jwtUtil.extractUsername(token)).thenThrow(mock(ExpiredJwtException.class));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // WICHTIG: Authentication muss null sein (nicht eingeloggt)
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // WICHTIG: Die Kette muss trotzdem weiterlaufen (für öffentliche Endpunkte)
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidTokenFormat_continuesWithoutAuth() throws ServletException, IOException {
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);

        // Mock wirft allgemeine JwtException (z.B. Signatur falsch)
        when(jwtUtil.extractUsername(token)).thenThrow(new JwtException("Invalid signature"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenValidButValidationFails_continuesWithoutAuth() throws ServletException, IOException {
        String token = "valid.token";
        String username = "testUser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        // Validierung schlägt fehl (z.B. Username passt nicht zum Token)
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}