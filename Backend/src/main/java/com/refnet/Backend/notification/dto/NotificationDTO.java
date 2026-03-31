package com.refnet.Backend.notification.dto;

import com.refnet.Backend.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private UUID id;
    private Notification.NotificationType type;
    private String title;
    private String body;
    private UUID referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
