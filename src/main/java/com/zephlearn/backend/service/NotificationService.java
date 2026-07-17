package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.CreateNotificationRequest;
import com.zephlearn.backend.dto.NotificationDTO;
import com.zephlearn.backend.model.Notification;
import com.zephlearn.backend.model.UserNotificationStatus;
import com.zephlearn.backend.repository.NotificationRepository;
import com.zephlearn.backend.repository.UserNotificationStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationStatusRepository statusRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserNotificationStatusRepository statusRepository) {
        this.notificationRepository = notificationRepository;
        this.statusRepository = statusRepository;
    }

    @Transactional
    public NotificationDTO createNotification(Long adminId, CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage().trim())
                .createdByAdminId(adminId)
                .build();

        notification = notificationRepository.save(notification);
        return toDto(notification, false, null);
    }

    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.findAllByOrderByCreatedAtDesc();
        Map<Long, UserNotificationStatus> statusByNotificationId = statusRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserNotificationStatus::getNotificationId, status -> status));

        return notifications.stream()
                .map(notification -> {
                    UserNotificationStatus status = statusByNotificationId.get(notification.getId());
                    boolean isRead = status != null && status.isRead();
                    LocalDateTime readAt = status != null ? status.getReadAt() : null;
                    return toDto(notification, isRead, readAt);
                })
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(n -> toDto(n, false, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDTO markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        UserNotificationStatus status = statusRepository
                .findByUserIdAndNotificationId(userId, notificationId)
                .orElseGet(() -> UserNotificationStatus.builder()
                        .userId(userId)
                        .notificationId(notificationId)
                        .build());

        status.setRead(true);
        status.setReadAt(LocalDateTime.now());
        statusRepository.save(status);

        return toDto(notification, true, status.getReadAt());
    }

    private NotificationDTO toDto(Notification notification, boolean isRead, LocalDateTime readAt) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdByAdminId(notification.getCreatedByAdminId())
                .createdAt(notification.getCreatedAt())
                .isRead(isRead)
                .readAt(readAt)
                .build();
    }
}
