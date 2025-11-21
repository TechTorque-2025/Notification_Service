package com.techtorque.notification_service.repository;

import com.techtorque.notification_service.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        testNotification = Notification.builder()
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
    void testSaveNotification() {
        Notification saved = notificationRepository.save(testNotification);

        assertThat(saved).isNotNull();
        assertThat(saved.getNotificationId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo("user123");
        assertThat(saved.getType()).isEqualTo(Notification.NotificationType.INFO);
        assertThat(saved.getRead()).isFalse();
    }

    @Test
    void testFindById() {
        notificationRepository.save(testNotification);

        Optional<Notification> found = notificationRepository.findById(testNotification.getNotificationId());

        assertThat(found).isPresent();
        assertThat(found.get().getMessage()).isEqualTo("Test notification");
    }

    @Test
    void testFindByUserIdAndDeletedFalse() {
        Notification notification2 = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.SUCCESS)
                .message("Second notification")
                .read(true)
                .deleted(false)
                .build();

        Notification notification3 = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.WARNING)
                .message("Deleted notification")
                .read(false)
                .deleted(true)
                .build();

        notificationRepository.save(testNotification);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        List<Notification> notifications = notificationRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc("user123");

        assertThat(notifications).hasSize(2);
        assertThat(notifications).noneMatch(Notification::getDeleted);
    }

    @Test
    void testFindByUserIdAndReadAndDeletedFalse() {
        Notification notification2 = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.SUCCESS)
                .message("Read notification")
                .read(true)
                .deleted(false)
                .build();

        notificationRepository.save(testNotification);
        notificationRepository.save(notification2);

        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndReadAndDeletedFalseOrderByCreatedAtDesc("user123", false);

        assertThat(unreadNotifications).hasSize(1);
        assertThat(unreadNotifications.get(0).getRead()).isFalse();
    }

    @Test
    void testFindUnreadNotificationsByUserId() {
        Notification notification2 = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.ERROR)
                .message("Unread notification 2")
                .read(false)
                .deleted(false)
                .build();

        Notification notification3 = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.INFO)
                .message("Read notification")
                .read(true)
                .deleted(false)
                .build();

        notificationRepository.save(testNotification);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        List<Notification> unreadNotifications = notificationRepository
                .findUnreadNotificationsByUserId("user123");

        assertThat(unreadNotifications).hasSize(2);
        assertThat(unreadNotifications).allMatch(n -> !n.getRead());
    }

    @Test
    void testCountUnreadByUserId() {
        Notification notification2 = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.SUCCESS)
                .message("Unread notification 2")
                .read(false)
                .deleted(false)
                .build();

        Notification notification3 = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.INFO)
                .message("Read notification")
                .read(true)
                .deleted(false)
                .build();

        notificationRepository.save(testNotification);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        Long unreadCount = notificationRepository.countUnreadByUserId("user123");

        assertThat(unreadCount).isEqualTo(2);
    }

    @Test
    void testFindExpiredNotifications() {
        Notification expiredNotification = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.INFO)
                .message("Expired notification")
                .read(false)
                .deleted(false)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        notificationRepository.save(testNotification);
        notificationRepository.save(expiredNotification);

        List<Notification> expiredNotifications = notificationRepository
                .findExpiredNotifications(LocalDateTime.now());

        assertThat(expiredNotifications).hasSize(1);
        assertThat(expiredNotifications.get(0).getExpiresAt())
                .isBefore(LocalDateTime.now());
    }

    @Test
    void testFindByRelatedEntityIdAndType() {
        Notification notification2 = Notification.builder()
                .userId("user456")
                .type(Notification.NotificationType.SERVICE_STARTED)
                .message("Service started")
                .read(false)
                .deleted(false)
                .relatedEntityId("entity123")
                .relatedEntityType("SERVICE")
                .build();

        notificationRepository.save(testNotification);
        notificationRepository.save(notification2);

        List<Notification> relatedNotifications = notificationRepository
                .findByRelatedEntityIdAndRelatedEntityType("entity123", "SERVICE");

        assertThat(relatedNotifications).hasSize(2);
        assertThat(relatedNotifications).allMatch(n -> n.getRelatedEntityId().equals("entity123"));
    }

    @Test
    void testUpdateNotification() {
        notificationRepository.save(testNotification);

        testNotification.setRead(true);
        testNotification.setReadAt(LocalDateTime.now());
        Notification updated = notificationRepository.save(testNotification);

        assertThat(updated.getRead()).isTrue();
        assertThat(updated.getReadAt()).isNotNull();
    }

    @Test
    void testDeleteNotification() {
        notificationRepository.save(testNotification);

        testNotification.setDeleted(true);
        notificationRepository.save(testNotification);

        List<Notification> activeNotifications = notificationRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc("user123");

        assertThat(activeNotifications).isEmpty();
    }

    @Test
    void testNotificationTypes() {
        Notification appointmentReminder = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.APPOINTMENT_REMINDER)
                .message("Appointment reminder")
                .read(false)
                .deleted(false)
                .build();

        Notification paymentReceived = Notification.builder()
                .userId("user123")
                .type(Notification.NotificationType.PAYMENT_RECEIVED)
                .message("Payment received")
                .read(false)
                .deleted(false)
                .build();

        notificationRepository.save(appointmentReminder);
        notificationRepository.save(paymentReceived);

        List<Notification> notifications = notificationRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc("user123");

        assertThat(notifications).hasSize(2);
        assertThat(notifications).anyMatch(n ->
                n.getType() == Notification.NotificationType.APPOINTMENT_REMINDER);
        assertThat(notifications).anyMatch(n ->
                n.getType() == Notification.NotificationType.PAYMENT_RECEIVED);
    }
}
