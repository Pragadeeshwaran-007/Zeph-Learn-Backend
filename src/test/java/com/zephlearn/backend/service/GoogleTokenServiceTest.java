package com.zephlearn.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GoogleTokenService.
 *
 * Strategy: GoogleTokenService builds a live GoogleIdTokenVerifier in its
 * constructor. We inject a Mockito mock of GoogleIdTokenVerifier via reflection
 * after construction so that no real HTTP call is ever made to Google's servers.
 *
 * This isolates the service logic (null-token check, blank-email check,
 * name-fallback behaviour) without any network dependency.
 */
@ExtendWith(MockitoExtension.class)
class GoogleTokenServiceTest {

    @Mock
    private GoogleIdTokenVerifier mockVerifier;

    @Mock
    private GoogleIdToken mockIdToken;

    @Mock
    private GoogleIdToken.Payload mockPayload;

    private GoogleTokenService googleTokenService;

    @BeforeEach
    void setUp() throws Exception {
        // Instantiate with a dummy client-id (doesn't matter — verifier is replaced)
        googleTokenService = new GoogleTokenService("dummy-client-id");

        // Inject the mock verifier via reflection so no network calls are made
        Field verifierField = GoogleTokenService.class.getDeclaredField("verifier");
        verifierField.setAccessible(true);
        verifierField.set(googleTokenService, mockVerifier);
    }

    // ---------------------------------------------------------------
    // verifyIdToken()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("should_ReturnGoogleUserInfo_When_TokenIsValid")
    void should_ReturnGoogleUserInfo_When_TokenIsValid() throws Exception {
        // Arrange
        when(mockVerifier.verify("valid-token")).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("alice@gmail.com");
        when(mockPayload.get("name")).thenReturn("Alice");
        when(mockPayload.getSubject()).thenReturn("google-uid-123");

        // Act
        GoogleTokenService.GoogleUserInfo info =
                googleTokenService.verifyIdToken("valid-token");

        // Assert
        assertThat(info.googleId()).isEqualTo("google-uid-123");
        assertThat(info.email()).isEqualTo("alice@gmail.com");
        assertThat(info.name()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("should_ThrowException_When_TokenIsInvalid")
    void should_ThrowException_When_TokenIsInvalid() throws Exception {
        // Arrange — verifier returns null for an invalid/expired token
        when(mockVerifier.verify("bad-token")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> googleTokenService.verifyIdToken("bad-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid Google ID token");
    }

    @Test
    @DisplayName("should_ThrowException_When_TokenEmailIsBlank")
    void should_ThrowException_When_TokenEmailIsBlank() throws Exception {
        // Arrange — token verifies but email is empty
        when(mockVerifier.verify("no-email-token")).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("  "); // blank

        // Act & Assert
        assertThatThrownBy(() -> googleTokenService.verifyIdToken("no-email-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email is not available");
    }

    @Test
    @DisplayName("should_FallbackToEmailPrefix_When_NameIsNull")
    void should_FallbackToEmailPrefix_When_NameIsNull() throws Exception {
        // Arrange — name field is null in payload; should derive from email
        when(mockVerifier.verify("no-name-token")).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("charlie@gmail.com");
        when(mockPayload.get("name")).thenReturn(null);   // no display name
        when(mockPayload.getSubject()).thenReturn("uid-999");

        // Act
        GoogleTokenService.GoogleUserInfo info =
                googleTokenService.verifyIdToken("no-name-token");

        // Assert — name should fall back to the portion before '@'
        assertThat(info.name()).isEqualTo("charlie");
    }

    @Test
    @DisplayName("should_WrapCheckedExceptions_When_VerifierThrowsIOException")
    void should_WrapCheckedExceptions_When_VerifierThrowsIOException() throws Exception {
        // Arrange — verifier.verify() declares IOException; simulate that
        when(mockVerifier.verify("io-error-token"))
                .thenThrow(new java.io.IOException("network failure"));

        // Act & Assert
        assertThatThrownBy(() -> googleTokenService.verifyIdToken("io-error-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to verify Google ID token");
    }
}
