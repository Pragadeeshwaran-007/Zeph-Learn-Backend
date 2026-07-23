package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.AuthRequest;
import com.zephlearn.backend.dto.AuthResponse;
import com.zephlearn.backend.dto.GoogleAuthRequest;
import com.zephlearn.backend.dto.LoginRequest;
import com.zephlearn.backend.model.AuthProvider;
import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;
import com.zephlearn.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * Strategy: pure Mockito, no Spring context needed.
 * Every external collaborator (UserRepository, PasswordEncoder, JwtUtil,
 * StreakService, GoogleTokenService) is mocked.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private StreakService streakService;
    @Mock private GoogleTokenService googleTokenService;

    @InjectMocks private AuthService authService;

    // ---------------------------------------------------------------
    // Common test data
    // ---------------------------------------------------------------
    private static final String EMAIL      = "alice@example.com";
    private static final String NAME       = "Alice";
    private static final String RAW_PASS   = "password123";
    private static final String HASHED     = "$2a$10$hashedPassword";
    private static final String JWT_TOKEN  = "mock.jwt.token";

    private User localUser;

    @BeforeEach
    void setUp() {
        localUser = User.builder()
                .id(1L)
                .name(NAME)
                .email(EMAIL)
                .password(HASHED)
                .authProvider(AuthProvider.LOCAL)
                .role("USER")
                .streak(0)
                .build();
    }

    // signup

    @Test
    @DisplayName("should_ReturnAuthResponse_When_SignupWithNewEmail")
    void should_ReturnAuthResponse_When_SignupWithNewEmail() {
        // Arrange
        AuthRequest request = new AuthRequest(NAME, EMAIL, RAW_PASS);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASS)).thenReturn(HASHED);
        when(userRepository.save(any(User.class))).thenReturn(localUser);
        when(jwtUtil.generateToken(EMAIL, "USER")).thenReturn(JWT_TOKEN);

        // Act
        AuthResponse response = authService.signup(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(JWT_TOKEN);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getName()).isEqualTo(NAME);
        assertThat(response.getRole()).isEqualTo("USER");
        verify(passwordEncoder).encode(RAW_PASS);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_SignupWithDuplicateEmail")
    void should_ThrowException_When_SignupWithDuplicateEmail() {
        // Arrange — email already exists
        AuthRequest request = new AuthRequest(NAME, EMAIL, RAW_PASS);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        // Verify that no user was saved
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should_HashPassword_When_SigningUp")
    void should_HashPassword_When_SigningUp() {
        // Arrange
        AuthRequest request = new AuthRequest(NAME, EMAIL, RAW_PASS);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASS)).thenReturn(HASHED);
        when(userRepository.save(any(User.class))).thenReturn(localUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(JWT_TOKEN);

        // Act
        authService.signup(request);

        // Assert — raw password was encoded, not stored as plain text
        verify(passwordEncoder).encode(RAW_PASS);
        verify(userRepository).save(argThat(u -> HASHED.equals(u.getPassword())));
    }

    // login


    @Test
    @DisplayName("should_ReturnJwtToken_When_LoginCredentialsAreValid")
    void should_ReturnJwtToken_When_LoginCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest(EMAIL, RAW_PASS);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches(RAW_PASS, HASHED)).thenReturn(true);
        when(streakService.updateStreak(localUser)).thenReturn(localUser);
        when(jwtUtil.generateToken(EMAIL, "USER")).thenReturn(JWT_TOKEN);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getToken()).isEqualTo(JWT_TOKEN);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("should_ThrowException_When_LoginEmailDoesNotExist")
    void should_ThrowException_When_LoginEmailDoesNotExist() {
        // Arrange
        LoginRequest request = new LoginRequest("nobody@example.com", RAW_PASS);
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("should_ThrowException_When_LoginPasswordIsWrong")
    void should_ThrowException_When_LoginPasswordIsWrong() {
        // Arrange
        LoginRequest request = new LoginRequest(EMAIL, "wrongPass");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches("wrongPass", HASHED)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("should_ThrowException_When_LoginAttemptedForGoogleAccount")
    void should_ThrowException_When_LoginAttemptedForGoogleAccount() {
        // Arrange — user registered via Google, has no local password
        User googleUser = User.builder()
                .id(2L).name("Bob").email(EMAIL)
                .password(null)
                .authProvider(AuthProvider.GOOGLE)
                .role("USER").streak(0).build();
        LoginRequest request = new LoginRequest(EMAIL, RAW_PASS);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(googleUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Google Sign-In");
    }

    @Test
    @DisplayName("should_UpdateStreakOnLogin_When_CredentialsAreValid")
    void should_UpdateStreakOnLogin_When_CredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest(EMAIL, RAW_PASS);
        User updatedUser = User.builder().id(1L).name(NAME).email(EMAIL)
                .password(HASHED).authProvider(AuthProvider.LOCAL)
                .role("USER").streak(5).build();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches(RAW_PASS, HASHED)).thenReturn(true);
        when(streakService.updateStreak(localUser)).thenReturn(updatedUser);
        when(jwtUtil.generateToken(EMAIL, "USER")).thenReturn(JWT_TOKEN);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getStreak()).isEqualTo(5);
        verify(streakService).updateStreak(localUser);
    }

    // googleLogin

    @Test
    @DisplayName("should_CreateNewUserAndReturnToken_When_GoogleLoginWithNewAccount")
    void should_CreateNewUserAndReturnToken_When_GoogleLoginWithNewAccount() {
        // Arrange
        GoogleAuthRequest request = new GoogleAuthRequest("google-id-token");
        GoogleTokenService.GoogleUserInfo googleInfo =
                new GoogleTokenService.GoogleUserInfo("google123", EMAIL, NAME);

        User savedGoogleUser = User.builder()
                .id(3L).name(NAME).email(EMAIL)
                .authProvider(AuthProvider.GOOGLE).googleId("google123")
                .role("USER").streak(1).build();

        when(googleTokenService.verifyIdToken("google-id-token")).thenReturn(googleInfo);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedGoogleUser);
        when(streakService.updateStreak(savedGoogleUser)).thenReturn(savedGoogleUser);
        when(jwtUtil.generateToken(EMAIL, "USER")).thenReturn(JWT_TOKEN);

        // Act
        AuthResponse response = authService.googleLogin(request);

        // Assert
        assertThat(response.getToken()).isEqualTo(JWT_TOKEN);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_GoogleTokenIsInvalid")
    void should_ThrowException_When_GoogleTokenIsInvalid() {
        // Arrange
        GoogleAuthRequest request = new GoogleAuthRequest("bad-token");
        when(googleTokenService.verifyIdToken("bad-token"))
                .thenThrow(new RuntimeException("Invalid Google ID token"));

        // Act & Assert
        assertThatThrownBy(() -> authService.googleLogin(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid Google ID token");
    }
}
