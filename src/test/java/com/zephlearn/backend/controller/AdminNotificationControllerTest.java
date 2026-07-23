package com.zephlearn.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zephlearn.backend.dto.CreateNotificationRequest;
import com.zephlearn.backend.dto.NotificationDTO;
import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;
import com.zephlearn.backend.security.CustomUserDetailsService;
import com.zephlearn.backend.security.JwtUtil;
import com.zephlearn.backend.security.SecurityConfig;
import com.zephlearn.backend.service.NotificationService;
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

@WebMvcTest(AdminNotificationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AdminNotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private NotificationService notificationService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("should_Return403Forbidden_When_NonAdminAttemptsToBroadcastNotification")
    void should_Return403Forbidden_When_NonAdminAttemptsToBroadcastNotification() throws Exception {
        CreateNotificationRequest req = new CreateNotificationRequest("Alert", "System Update");

        mockMvc.perform(post("/api/admin/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("should_AllowBroadcastNotification_When_UserIsAdmin")
    void should_AllowBroadcastNotification_When_UserIsAdmin() throws Exception {
        CreateNotificationRequest req = new CreateNotificationRequest("Alert", "System Update");
        User admin = User.builder().id(99L).email("admin@example.com").role("ADMIN").build();
        NotificationDTO dto = NotificationDTO.builder().id(1L).title("Alert").message("System Update").createdByAdminId(99L).build();

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(notificationService.createNotification(eq(99L), any(CreateNotificationRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/admin/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alert"))
                .andExpect(jsonPath("$.createdByAdminId").value(99));
    }
}
