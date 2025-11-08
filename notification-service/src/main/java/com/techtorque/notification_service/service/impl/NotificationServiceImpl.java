package com.techtorque.notification_service.service.impl;

import com.techtorque.notification_service.dto.response.NotificationResponse;
import com.techtorque.notification_service.entity.Notification;
import com.techtorque.notification_service.repository.NotificationRepository;
import com.techtorque.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(String userId, Boolean unreadOnly) {
        log.info("Fetching notifications for user: {}, unreadOnly: {}", userId, unreadOnly);
        
        List<Notification> notifications;
        if (unreadOnly != null && unreadOnly) {
            notifications = notificationRepository.findByUserIdAndReadAndDeletedFalseOrderByCreatedAtDesc(userId, false);
        } else {
            notifications = notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
        }
        
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String notificationId, String userId, Boolean read) {
        log.info("Marking notification {} as read: {} for user: {}", notificationId, read, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notification");
        }
        
        notification.setRead(read);
        if (read) {
            notification.setReadAt(LocalDateTime.now());
        } else {
            notification.setReadAt(null);
        }
        
        Notification updated = notificationRepository.save(notification);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        log.info("Deleting notification {} for user: {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notification");
        }
        
        notification.setDeleted(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(String userId, Notification.NotificationType type, 
                                                   String message, String details) {
        log.info("Creating notification for user: {}, type: {}", userId, type);
        
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .details(details)
                .read(false)
                .deleted(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        
        Notification saved = notificationRepository.save(notification);
        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteExpiredNotifications() {
        log.info("Deleting expired notifications");
        List<Notification> expired = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        expired.forEach(n -> n.setDeleted(true));
        notificationRepository.saveAll(expired);
        log.info("Deleted {} expired notifications", expired.size());
    }

    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType().name())
                .message(notification.getMessage())
                .details(notification.getDetails())
                .read(notification.getRead())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .expiresAt(notification.getExpiresAt())
                .build();
    }
}
