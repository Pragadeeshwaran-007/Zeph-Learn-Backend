package com.zephlearn.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "super_secret_jwt_key_for_zephlearn_test_environment_12345";
    private static final long EXPIRATION = 3600000; // 1 hour

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION);
    }

    @Test
    @DisplayName("should_GenerateValidJwtToken_When_EmailAndRoleProvided")
    void should_GenerateValidJwtToken_When_EmailAndRoleProvided() {
        String token = jwtUtil.generateToken("user@example.com", "USER");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("USER");
    }

    @Test
    @DisplayName("should_RejectExpiredToken_When_TokenIsExpired")
    void should_RejectExpiredToken_When_TokenIsExpired() {
        // Create an expired token manually using the same key
        JwtUtil shortLivedJwtUtil = new JwtUtil(SECRET, -1000); // expired 1s ago
        String expiredToken = shortLivedJwtUtil.generateToken("user@example.com", "USER");

        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("should_RejectTamperedToken_When_SignatureIsModified")
    void should_RejectTamperedToken_When_SignatureIsModified() {
        String validToken = jwtUtil.generateToken("user@example.com", "USER");
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "abcde";

        assertThat(jwtUtil.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("should_RejectTokenSignedWithDifferentSecret")
    void should_RejectTokenSignedWithDifferentSecret() {
        JwtUtil anotherJwtUtil = new JwtUtil("different_secret_key_that_is_at_least_32_bytes_long_12345", EXPIRATION);
        String tokenFromOther = anotherJwtUtil.generateToken("user@example.com", "USER");

        assertThat(jwtUtil.validateToken(tokenFromOther)).isFalse();
    }
}
