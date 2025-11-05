package com.techtorque.notification_service.service;

import com.techtorque.notification_service.dto.response.NotificationResponse;
import com.techtorque.notification_service.entity.Notification;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getUserNotifications(String userId, Boolean unreadOnly);

    NotificationResponse markAsRead(String notificationId, String userId, Boolean read);

    void deleteNotification(String notificationId, String userId);

    NotificationResponse createNotification(String userId, Notification.NotificationType type, String message, String details);

    Long getUnreadCount(String userId);

    void deleteExpiredNotifications();
}
