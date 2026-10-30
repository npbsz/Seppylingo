package com.sep.sep_backend.security;

import com.sep.sep_backend.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException; // WICHTIG: Dieser Import muss da sein!
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // 1. Token aus Header extrahieren (Format: "Bearer <token>")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                // WICHTIG: Abgelaufene Token loggen wir nur als Info, nicht als Error.
                // Wir lassen den Request weiterlaufen (filterChain), da die Route (z.B. /api/ai) evtl. öffentlich ist.
                logger.info("JWT ist abgelaufen. Request läuft anonym weiter.");
            } catch (Exception e) {
                // Echte Fehler (z.B. Signatur manipuliert) loggen wir weiterhin
                logger.error("Fehler beim Verarbeiten des JWT Tokens: " + e.getMessage());
            }
        }

        // 2. Authentifizierung durchführen, falls noch nicht geschehen und Username gefunden wurde
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Hier nochmal try-catch, falls validateToken Exceptions wirft
            try {
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Setze den authentifizierten User in den Context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                 logger.error("Token Validierung fehlgeschlagen", e);
            }
        }

        // Anfrage weiterschicken (auch wenn kein Token da war oder er abgelaufen ist)
        filterChain.doFilter(request, response);
    }
}