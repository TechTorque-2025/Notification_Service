package com.techtorque.notification_service.controller;

import com.techtorque.notification_service.dto.request.CreateNotificationRequest;
import com.techtorque.notification_service.dto.response.ApiResponse;
import com.techtorque.notification_service.dto.response.NotificationResponse;
import com.techtorque.notification_service.entity.Notification;
import com.techtorque.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * API Controller for other services to create notifications
 * This is separate from the user-facing NotificationController
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationApiController {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create a notification (called by other microservices)
     * This endpoint is whitelisted in SecurityConfig for service-to-service communication
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        log.info("Creating notification for user: {} with type: {}", request.getUserId(), request.getType());

        try {
            // Parse notification type
            Notification.NotificationType notificationType = Notification.NotificationType.valueOf(request.getType());

            // Create notification using service
            NotificationResponse response = notificationService.createNotification(
                    request.getUserId(),
                    notificationType,
                    request.getMessage(),
                    request.getDetails()
            );

            // Send real-time notification via WebSocket
            String destination = "/user/" + request.getUserId() + "/queue/notifications";
            log.info("Sending WebSocket notification to destination: {}", destination);
            messagingTemplate.convertAndSend(destination, response);

            log.info("Notification created successfully for user: {}", request.getUserId());
            return ResponseEntity.ok(ApiResponse.success("Notification created", response));

        } catch (IllegalArgumentException e) {
            log.error("Invalid notification type: {}", request.getType(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message("Invalid notification type: " + request.getType())
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create notification for user: {}", request.getUserId(), e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.builder()
                            .message("Failed to create notification")
                            .build()
            );
        }
    }
}
