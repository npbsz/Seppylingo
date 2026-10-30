package com.sep.sep_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "MeinSuperGeheimesJwtSecretKeyDasMindestens32BytesLangSeinMuss!".getBytes());
    private final long expirationMs = 86400000;

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    // Diese Methode wird vom Filter unter dem Namen "extractUsername" gesucht
    public String extractUsername(String token) {
        return getUsernameFromToken(token);
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Einfache Validierung (Signatur & Ablauf)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Validierung mit UserDetails (vom Filter benötigt)
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Prüft, ob der Token zum User gehört UND ob er gültig (nicht abgelaufen) ist
        return (username.equals(userDetails.getUsername()) && validateToken(token));
    }
}