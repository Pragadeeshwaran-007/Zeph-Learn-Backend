package com.zephlearn.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long jwtExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret, 
                   @Value("${jwt.expiration}") long jwtExpiration) {
        // Ensure secret is long enough to meet HS256 requirements
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if(keyBytes.length < 32){
            String padded = secret + "00000000000000000000000000000000";
            this.key = Keys.hmacShaKeyFor(padded.substring(0, 32).getBytes(StandardCharsets.UTF_8));
        } else {
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
