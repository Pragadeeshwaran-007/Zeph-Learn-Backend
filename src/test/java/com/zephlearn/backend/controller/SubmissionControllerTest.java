package com.zephlearn.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zephlearn.backend.dto.SubmissionRequest;
import com.zephlearn.backend.dto.SubmitResponse;
import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;
import com.zephlearn.backend.security.CustomUserDetailsService;
import com.zephlearn.backend.security.JwtUtil;
import com.zephlearn.backend.security.SecurityConfig;
import com.zephlearn.backend.service.SubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(SubmissionController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SubmissionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private SubmissionService submissionService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("should_RejectUnauthenticatedRequest_When_SubmittingWithoutAuth")
    void should_RejectUnauthenticatedRequest_When_SubmittingWithoutAuth() throws Exception {
        SubmissionRequest request = new SubmissionRequest(1L, "print(1)", "python", null, null);

        mockMvc.perform(post("/api/submissions/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = "USER")
    @DisplayName("should_AllowAuthenticatedSubmit_When_UserIsLoggedIn")
    void should_AllowAuthenticatedSubmit_When_UserIsLoggedIn() throws Exception {
        SubmissionRequest request = new SubmissionRequest(1L, "print(1)", "python", null, null);
        User user = User.builder().id(10L).email("alice@example.com").build();
        SubmitResponse response = new SubmitResponse();
        response.setVerdict("Accepted");
        response.setPassedCount(1);
        response.setTotalCount(1);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(submissionService.submit(eq(10L), any(SubmissionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/submissions/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("Accepted"))
                .andExpect(jsonPath("$.passedCount").value(1));
    }
}
