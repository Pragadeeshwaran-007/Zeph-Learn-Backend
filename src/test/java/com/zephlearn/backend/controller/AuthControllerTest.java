package com.zephlearn.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zephlearn.backend.dto.AuthRequest;
import com.zephlearn.backend.dto.AuthResponse;
import com.zephlearn.backend.dto.GoogleAuthRequest;
import com.zephlearn.backend.dto.LoginRequest;
import com.zephlearn.backend.security.CustomUserDetailsService;
import com.zephlearn.backend.security.JwtUtil;
import com.zephlearn.backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("should_Return200AndAuthResponse_When_SignupWithValidPayload")
    void should_Return200AndAuthResponse_When_SignupWithValidPayload() throws Exception {
        AuthRequest req = new AuthRequest("Alice", "alice@example.com", "password123");
        AuthResponse res = AuthResponse.builder()
                .token("jwt-token-123")
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .role("USER")
                .build();

        when(authService.signup(any(AuthRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("should_Return400BadRequest_When_SignupPayloadIsInvalid")
    void should_Return400BadRequest_When_SignupPayloadIsInvalid() throws Exception {
        AuthRequest req = new AuthRequest("", "invalid-email", "short"); // invalid name, email, short password

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should_Return200AndAuthResponse_When_LoginWithValidCredentials")
    void should_Return200AndAuthResponse_When_LoginWithValidCredentials() throws Exception {
        LoginRequest req = new LoginRequest("alice@example.com", "password123");
        AuthResponse res = AuthResponse.builder()
                .token("jwt-token-123")
                .id(1L)
                .email("alice@example.com")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    @DisplayName("should_Return200AndAuthResponse_When_GoogleLoginWithValidIdToken")
    void should_Return200AndAuthResponse_When_GoogleLoginWithValidIdToken() throws Exception {
        GoogleAuthRequest req = new GoogleAuthRequest("valid-google-id-token");
        AuthResponse res = AuthResponse.builder()
                .token("jwt-token-google")
                .email("googleuser@example.com")
                .build();

        when(authService.googleLogin(any(GoogleAuthRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-google"));
    }
}
