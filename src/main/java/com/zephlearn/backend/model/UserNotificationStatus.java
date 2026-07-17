package com.zephlearn.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_notification_status",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notification_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long notificationId;

    @Builder.Default
    private boolean isRead = false;

    private LocalDateTime readAt;
}
