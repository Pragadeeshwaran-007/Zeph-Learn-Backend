package com.zephlearn.backend.controller;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private NotificationService notificationService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("should_RejectUnauthenticatedRequest_When_FetchingNotificationsWithoutAuth")
    void should_RejectUnauthenticatedRequest_When_FetchingNotificationsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("should_ReturnUserNotifications_When_AuthenticatedUserRequests")
    void should_ReturnUserNotifications_When_AuthenticatedUserRequests() throws Exception {
        User user = User.builder().id(5L).email("user@example.com").build();
        NotificationDTO dto = NotificationDTO.builder().id(1L).title("Welcome").message("Hello").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationService.getNotificationsForUser(5L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Welcome"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("should_MarkNotificationAsRead_When_AuthenticatedUserCallsMarkAsRead")
    void should_MarkNotificationAsRead_When_AuthenticatedUserCallsMarkAsRead() throws Exception {
        User user = User.builder().id(5L).email("user@example.com").build();
        NotificationDTO dto = NotificationDTO.builder().id(1L).title("Welcome").isRead(true).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(notificationService.markAsRead(5L, 1L)).thenReturn(dto);

        mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }
}
