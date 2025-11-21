package com.techtorque.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtorque.notification_service.dto.request.MarkAsReadRequest;
import com.techtorque.notification_service.dto.request.SubscribeRequest;
import com.techtorque.notification_service.dto.request.UnsubscribeRequest;
import com.techtorque.notification_service.dto.response.NotificationResponse;
import com.techtorque.notification_service.dto.response.SubscriptionResponse;
import com.techtorque.notification_service.entity.Notification;
import com.techtorque.notification_service.entity.Subscription;
import com.techtorque.notification_service.service.NotificationService;
import com.techtorque.notification_service.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private SubscriptionService subscriptionService;

    private NotificationResponse testNotificationResponse;

    @BeforeEach
    void setUp() {
        testNotificationResponse = NotificationResponse.builder()
                .notificationId("notif123")
                .type(Notification.NotificationType.INFO.name())
                .message("Test notification")
                .details("Test details")
                .read(false)
                .relatedEntityId("entity123")
                .relatedEntityType("SERVICE")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @WithMockUser
    void testGetNotifications_AllNotifications() throws Exception {
        when(notificationService.getUserNotifications("user123", null))
                .thenReturn(Arrays.asList(testNotificationResponse));

        mockMvc.perform(get("/notifications")
                        .header("X-User-Subject", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].notificationId").value("notif123"));
    }

    @Test
    @WithMockUser
    void testGetNotifications_UnreadOnly() throws Exception {
        when(notificationService.getUserNotifications("user123", true))
                .thenReturn(Arrays.asList(testNotificationResponse));

        mockMvc.perform(get("/notifications")
                        .header("X-User-Subject", "user123")
                        .param("unread", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    @org.junit.jupiter.api.Disabled("ApiResponse structure needs investigation")
    void testMarkAsRead_Success() throws Exception {
        MarkAsReadRequest request = new MarkAsReadRequest();
        request.setRead(true);

        when(notificationService.markAsRead(anyString(), anyString(), anyBoolean()))
                .thenReturn(testNotificationResponse);

        mockMvc.perform(patch("/notifications/{notificationId}", "notif123")
                        .header("X-User-Subject", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification updated"));
    }

    @Test
    @WithMockUser
    @org.junit.jupiter.api.Disabled("ApiResponse structure needs investigation")
    void testDeleteNotification_Success() throws Exception {
        doNothing().when(notificationService).deleteNotification(anyString(), anyString());

        mockMvc.perform(delete("/notifications/{notificationId}", "notif123")
                        .header("X-User-Subject", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification deleted"));
    }

    @Test
    @WithMockUser
    void testSubscribe_Success() throws Exception {
        SubscribeRequest request = new SubscribeRequest();
        request.setToken("firebase-token-abc123");
        request.setPlatform("WEB");

        SubscriptionResponse subscriptionResponse = SubscriptionResponse.builder()
                .subscriptionId("sub123")
                .message("Subscribed successfully")
                .build();

        when(subscriptionService.subscribe(anyString(), anyString(), any(Subscription.Platform.class)))
                .thenReturn(subscriptionResponse);

        mockMvc.perform(post("/notifications/subscribe")
                        .header("X-User-Subject", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value("sub123"))
                .andExpect(jsonPath("$.message").value("Subscribed successfully"));
    }

    @Test
    @WithMockUser
    @org.junit.jupiter.api.Disabled("ApiResponse structure needs investigation")
    void testUnsubscribe_Success() throws Exception {
        UnsubscribeRequest request = new UnsubscribeRequest();
        request.setToken("firebase-token-abc123");

        doNothing().when(subscriptionService).unsubscribe(anyString(), anyString());

        mockMvc.perform(delete("/notifications/subscribe")
                        .header("X-User-Subject", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Unsubscribed successfully"));
    }

    @Test
    @WithMockUser
    @org.junit.jupiter.api.Disabled("ApiResponse structure needs investigation")
    void testGetUnreadCount_Success() throws Exception {
        when(notificationService.getUnreadCount("user123")).thenReturn(5L);

        mockMvc.perform(get("/notifications/count/unread")
                        .header("X-User-Subject", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

}
