package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.CreateNotificationRequest;
import com.zephlearn.backend.dto.NotificationDTO;
import com.zephlearn.backend.model.Notification;
import com.zephlearn.backend.model.UserNotificationStatus;
import com.zephlearn.backend.repository.NotificationRepository;
import com.zephlearn.backend.repository.UserNotificationStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserNotificationStatusRepository statusRepository;

    @InjectMocks private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(1L)
                .title("Maintenance")
                .message("Server restart at 12 PM")
                .createdByAdminId(99L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("should_CreateNotification_When_ValidRequestGiven")
    void should_CreateNotification_When_ValidRequestGiven() {
        CreateNotificationRequest req = new CreateNotificationRequest("Maintenance", "Server restart at 12 PM");

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationDTO dto = notificationService.createNotification(99L, req);

        assertThat(dto.getTitle()).isEqualTo("Maintenance");
        assertThat(dto.getMessage()).isEqualTo("Server restart at 12 PM");
        assertThat(dto.getCreatedByAdminId()).isEqualTo(99L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("should_GetNotificationsForUser_WithCorrectReadStatus")
    void should_GetNotificationsForUser_WithCorrectReadStatus() {
        UserNotificationStatus status = UserNotificationStatus.builder()
                .id(10L)
                .userId(5L)
                .notificationId(1L)
                .isRead(true)
                .readAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(notification));
        when(statusRepository.findByUserId(5L)).thenReturn(List.of(status));

        List<NotificationDTO> dtos = notificationService.getNotificationsForUser(5L);

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).isRead()).isTrue();
        assertThat(dtos.get(0).getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("should_MarkAsRead_When_ValidUserAndNotificationIdGiven")
    void should_MarkAsRead_When_ValidUserAndNotificationIdGiven() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(statusRepository.findByUserIdAndNotificationId(5L, 1L)).thenReturn(Optional.empty());
        when(statusRepository.save(any(UserNotificationStatus.class))).thenAnswer(i -> i.getArgument(0));

        NotificationDTO dto = notificationService.markAsRead(5L, 1L);

        assertThat(dto.isRead()).isTrue();
        assertThat(dto.getReadAt()).isNotNull();
        verify(statusRepository).save(argThat(UserNotificationStatus::isRead));
    }

    @Test
    @DisplayName("should_ThrowException_When_MarkingNonExistentNotificationAsRead")
    void should_ThrowException_When_MarkingNonExistentNotificationAsRead() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(5L, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");
    }
}
