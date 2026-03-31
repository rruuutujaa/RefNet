package com.refnet.Backend.notification.service;

import com.refnet.Backend.common.exception.AppException;
import com.refnet.Backend.common.exception.ResourceNotFoundException;
import com.refnet.Backend.notification.dto.NotificationDTO;
import com.refnet.Backend.notification.entity.Notification;
import com.refnet.Backend.notification.repository.NotificationRepository;
import com.refnet.Backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getMyNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserId(user.getId(), pageable)
                .map(this::mapToDTO);
    }

    @Cacheable(value = "unread_notification_count", key = "#user.id")
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    @CacheEvict(value = "unread_notification_count", key = "#user.id")
    public void markAsRead(User user, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AppException("Unauthorized to access this notification.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    @CacheEvict(value = "unread_notification_count", key = "#user.id")
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByUserId(user.getId());
    }

    @Async("notificationExecutor")
    @Transactional
    @CacheEvict(value = "unread_notification_count", key = "#user.id")
    public void createNotification(User user, Notification.NotificationType type, String title, String body, UUID referenceId) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .body(body)
                    .referenceId(referenceId)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            
            // Also trigger email
            emailService.sendEmail(user.getEmail(), title, body);
            
            log.info("Notification and email sent to user: {} (Type: {})", user.getEmail(), type);
        } catch (Exception e) {
            log.error("Failed to create async notification for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
