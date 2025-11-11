package com.techtorque.notification_service.controller;

import com.techtorque.notification_service.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time notification broadcasting
 *
 * Message destinations:
 * - /app/notifications.subscribe -> Client subscribes to notifications
 * - /topic/notifications -> Broadcast to all connected clients
 * - /user/{userId}/queue/notifications -> Send to specific user
 *
 * Industry standard WebSocket messaging with STOMP protocol
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle client subscription to notification stream
     * Clients send message to /app/notifications.subscribe
     */
    @MessageMapping("/notifications.subscribe")
    @SendTo("/topic/notifications")
    public String subscribe(@Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
        // Store userId in session attributes for user-specific routing
        headerAccessor.getSessionAttributes().put("userId", userId);
        log.info("User {} subscribed to WebSocket notifications", userId);
        return "Subscription successful";
    }

    /**
     * Send notification to specific user
     * Called by service layer when new notification is created
     *
     * @param userId Target user ID
     * @param notification Notification to send
     */
    public void sendNotificationToUser(String userId, NotificationResponse notification) {
        log.info("Sending WebSocket notification to user: {} - {}", userId, notification.getMessage());

        // Send to user-specific queue: /user/{userId}/queue/notifications
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            notification
        );
    }

    /**
     * Broadcast notification to all connected users
     * Used for system-wide announcements
     *
     * @param notification Notification to broadcast
     */
    public void broadcastNotification(NotificationResponse notification) {
        log.info("Broadcasting WebSocket notification: {}", notification.getMessage());

        // Send to topic: /topic/notifications (all subscribers receive)
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Send notification count update to user
     * Called when unread count changes
     *
     * @param userId Target user ID
     * @param count New unread count
     */
    public void sendUnreadCountUpdate(String userId, Long count) {
        log.debug("Sending unread count update to user {}: {}", userId, count);

        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications/count",
            count
        );
    }
}
