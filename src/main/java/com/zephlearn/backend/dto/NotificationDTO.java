package com.zephlearn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private Long createdByAdminId;
    private LocalDateTime createdAt;
    private boolean isRead;
    private LocalDateTime readAt;
}
