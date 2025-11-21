package com.techtorque.notification_service.service;

import com.techtorque.notification_service.controller.WebSocketNotificationController;
import com.techtorque.notification_service.dto.response.NotificationResponse;
import com.techtorque.notification_service.entity.Notification;
import com.techtorque.notification_service.repository.NotificationRepository;
import com.techtorque.notification_service.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WebSocketNotificationController webSocketController;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
                .notificationId("notif123")
                .userId("user123")
                .type(Notification.NotificationType.INFO)
                .message("Test notification")
                .details("Test details")
                .read(false)
                .deleted(false)
                .relatedEntityId("entity123")
                .relatedEntityType("SERVICE")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    void testGetUserNotifications_AllNotifications() {
        Notification notification2 = Notification.builder()
                .notificationId("notif456")
                .userId("user123")
                .type(Notification.NotificationType.SUCCESS)
                .message("Success notification")
                .read(true)
                .deleted(false)
                .build();

        when(notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc("user123"))
                .thenReturn(Arrays.asList(testNotification, notification2));

        List<NotificationResponse> responses = notificationService.getUserNotifications("user123", null);

        assertThat(responses).hasSize(2);
        verify(notificationRepository, times(1))
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc("user123");
    }

    @Test
    void testGetUserNotifications_UnreadOnly() {
        when(notificationRepository.findByUserIdAndReadAndDeletedFalseOrderByCreatedAtDesc("user123", false))
                .thenReturn(Arrays.asList(testNotification));

        List<NotificationResponse> responses = notificationService.getUserNotifications("user123", true);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRead()).isFalse();
        verify(notificationRepository, times(1))
                .findByUserIdAndReadAndDeletedFalseOrderByCreatedAtDesc("user123", false);
    }

    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById("notif123")).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationRepository.countUnreadByUserId("user123")).thenReturn(5L);
        doNothing().when(webSocketController).sendUnreadCountUpdate(anyString(), anyLong());

        NotificationResponse response = notificationService.markAsRead("notif123", "user123", true);

        assertThat(response).isNotNull();
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(webSocketController, times(1)).sendUnreadCountUpdate("user123", 5L);
    }

    @Test
    void testMarkAsRead_UnauthorizedAccess() {
        when(notificationRepository.findById("notif123")).thenReturn(Optional.of(testNotification));

        assertThatThrownBy(() -> notificationService.markAsRead("notif123", "wrongUser", true))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized access");
    }

    @Test
    void testMarkAsRead_NotificationNotFound() {
        when(notificationRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead("nonexistent", "user123", true))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");
    }

    @Test
    void testMarkAsUnread() {
        testNotification.setRead(true);
        testNotification.setReadAt(LocalDateTime.now());

        when(notificationRepository.findById("notif123")).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            assertThat(saved.getRead()).isFalse();
            assertThat(saved.getReadAt()).isNull();
            return saved;
        });
        when(notificationRepository.countUnreadByUserId("user123")).thenReturn(1L);
        doNothing().when(webSocketController).sendUnreadCountUpdate(anyString(), anyLong());

        notificationService.markAsRead("notif123", "user123", false);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testDeleteNotification_Success() {
        when(notificationRepository.findById("notif123")).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.deleteNotification("notif123", "user123");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testDeleteNotification_UnauthorizedAccess() {
        when(notificationRepository.findById("notif123")).thenReturn(Optional.of(testNotification));

        assertThatThrownBy(() -> notificationService.deleteNotification("notif123", "wrongUser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized access");
    }

    @Test
    void testCreateNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationRepository.countUnreadByUserId("user123")).thenReturn(1L);
        doNothing().when(webSocketController).sendNotificationToUser(anyString(), any());
        doNothing().when(webSocketController).sendUnreadCountUpdate(anyString(), anyLong());

        NotificationResponse response = notificationService.createNotification(
                "user123",
                Notification.NotificationType.INFO,
                "Test notification",
                "Test details"
        );

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Test notification");
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(webSocketController, times(1)).sendNotificationToUser(eq("user123"), any());
        verify(webSocketController, times(1)).sendUnreadCountUpdate("user123", 1L);
    }

    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countUnreadByUserId("user123")).thenReturn(5L);

        Long count = notificationService.getUnreadCount("user123");

        assertThat(count).isEqualTo(5L);
        verify(notificationRepository, times(1)).countUnreadByUserId("user123");
    }

    @Test
    void testDeleteExpiredNotifications() {
        Notification expiredNotif1 = Notification.builder()
                .notificationId("exp1")
                .userId("user123")
                .type(Notification.NotificationType.INFO)
                .message("Expired 1")
                .read(false)
                .deleted(false)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        Notification expiredNotif2 = Notification.builder()
                .notificationId("exp2")
                .userId("user456")
                .type(Notification.NotificationType.WARNING)
                .message("Expired 2")
                .read(false)
                .deleted(false)
                .expiresAt(LocalDateTime.now().minusDays(2))
                .build();

        List<Notification> expiredNotifications = Arrays.asList(expiredNotif1, expiredNotif2);

        when(notificationRepository.findExpiredNotifications(any(LocalDateTime.class)))
                .thenReturn(expiredNotifications);
        when(notificationRepository.saveAll(anyList())).thenReturn(expiredNotifications);

        notificationService.deleteExpiredNotifications();

        verify(notificationRepository, times(1)).findExpiredNotifications(any(LocalDateTime.class));
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateNotification_DifferentTypes() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationRepository.countUnreadByUserId(anyString())).thenReturn(1L);
        doNothing().when(webSocketController).sendNotificationToUser(anyString(), any());
        doNothing().when(webSocketController).sendUnreadCountUpdate(anyString(), anyLong());

        notificationService.createNotification(
                "user123",
                Notification.NotificationType.APPOINTMENT_REMINDER,
                "Appointment reminder",
                null
        );

        notificationService.createNotification(
                "user123",
                Notification.NotificationType.PAYMENT_RECEIVED,
                "Payment received",
                "Amount: $100"
        );

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
}
