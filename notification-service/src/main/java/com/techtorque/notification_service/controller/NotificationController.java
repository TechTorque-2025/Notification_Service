package com.techtorque.notification_service.controller;

import com.techtorque.notification_service.dto.request.MarkAsReadRequest;
import com.techtorque.notification_service.dto.request.SubscribeRequest;
import com.techtorque.notification_service.dto.request.UnsubscribeRequest;
import com.techtorque.notification_service.dto.response.ApiResponse;
import com.techtorque.notification_service.dto.response.NotificationResponse;
import com.techtorque.notification_service.dto.response.SubscriptionResponse;
import com.techtorque.notification_service.entity.Subscription;
import com.techtorque.notification_service.service.NotificationService;
import com.techtorque.notification_service.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(required = false) Boolean unread,
            HttpServletRequest request) {
        
        String userId = extractUserIdFromGateway(request);
        log.info("Getting notifications for user: {}, unreadOnly: {}", userId, unread);
        
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId, unread);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable String notificationId,
            @Valid @RequestBody MarkAsReadRequest request,
            HttpServletRequest httpRequest) {
        
        String userId = extractUserIdFromGateway(httpRequest);
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        
        NotificationResponse response = notificationService.markAsRead(notificationId, userId, request.getRead());
        return ResponseEntity.ok(ApiResponse.success("Notification updated", response));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse> deleteNotification(
            @PathVariable String notificationId,
            HttpServletRequest request) {
        
        String userId = extractUserIdFromGateway(request);
        log.info("Deleting notification {} for user: {}", notificationId, userId);
        
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody SubscribeRequest request,
            HttpServletRequest httpRequest) {
        
        String userId = extractUserIdFromGateway(httpRequest);
        log.info("User {} subscribing to push notifications", userId);
        
        Subscription.Platform platform = Subscription.Platform.valueOf(request.getPlatform());
        SubscriptionResponse response = subscriptionService.subscribe(userId, request.getToken(), platform);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<ApiResponse> unsubscribe(
            @Valid @RequestBody UnsubscribeRequest request,
            HttpServletRequest httpRequest) {
        
        String userId = extractUserIdFromGateway(httpRequest);
        log.info("User {} unsubscribing from push notifications", userId);
        
        subscriptionService.unsubscribe(userId, request.getToken());
        return ResponseEntity.ok(ApiResponse.success("Unsubscribed successfully"));
    }

    @GetMapping("/count/unread")
    public ResponseEntity<ApiResponse> getUnreadCount(HttpServletRequest request) {
        String userId = extractUserIdFromGateway(request);
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", count));
    }

    private String extractUserIdFromGateway(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Subject");
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("User ID not found in request headers. Request must come through API Gateway.");
        }
        return userId;
    }
}
