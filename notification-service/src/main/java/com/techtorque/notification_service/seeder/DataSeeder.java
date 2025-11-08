package com.techtorque.notification_service.seeder;

import com.techtorque.notification_service.entity.Notification;
import com.techtorque.notification_service.entity.Subscription;
import com.techtorque.notification_service.repository.NotificationRepository;
import com.techtorque.notification_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public void run(String... args) {
        log.info("Starting DataSeeder for Notification Service...");
        
        if (notificationRepository.count() == 0) {
            seedNotifications();
        } else {
            log.info("Notifications already seeded. Skipping...");
        }
        
        if (subscriptionRepository.count() == 0) {
            seedSubscriptions();
        } else {
            log.info("Subscriptions already seeded. Skipping...");
        }
        
        log.info("DataSeeder completed successfully!");
    }

    private void seedNotifications() {
        log.info("Seeding notifications...");
        
        String testUserId1 = UUID.randomUUID().toString();
        String testUserId2 = UUID.randomUUID().toString();
        
        List<Notification> notifications = Arrays.asList(
                Notification.builder()
                        .userId(testUserId1)
                        .type(Notification.NotificationType.APPOINTMENT_CONFIRMED)
                        .message("Your appointment has been confirmed")
                        .details("Appointment scheduled for tomorrow at 10:00 AM")
                        .read(false)
                        .deleted(false)
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build(),
                
                Notification.builder()
                        .userId(testUserId1)
                        .type(Notification.NotificationType.SERVICE_COMPLETED)
                        .message("Your service has been completed")
                        .details("Oil change and tire rotation completed successfully")
                        .read(true)
                        .deleted(false)
                        .readAt(LocalDateTime.now().minusHours(2))
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build(),
                
                Notification.builder()
                        .userId(testUserId1)
                        .type(Notification.NotificationType.INVOICE_GENERATED)
                        .message("Invoice generated for your service")
                        .details("Total amount: $150.00. Payment due in 7 days.")
                        .read(false)
                        .deleted(false)
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build(),
                
                Notification.builder()
                        .userId(testUserId2)
                        .type(Notification.NotificationType.APPOINTMENT_REMINDER)
                        .message("Appointment reminder")
                        .details("Your appointment is in 1 hour")
                        .read(false)
                        .deleted(false)
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .build(),
                
                Notification.builder()
                        .userId(testUserId2)
                        .type(Notification.NotificationType.PAYMENT_RECEIVED)
                        .message("Payment received")
                        .details("We've received your payment of $200.00")
                        .read(true)
                        .deleted(false)
                        .readAt(LocalDateTime.now().minusDays(1))
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build()
        );
        
        notificationRepository.saveAll(notifications);
        log.info("Seeded {} notifications", notifications.size());
    }

    private void seedSubscriptions() {
        log.info("Seeding subscriptions...");
        
        String testUserId1 = UUID.randomUUID().toString();
        String testUserId2 = UUID.randomUUID().toString();
        
        List<Subscription> subscriptions = Arrays.asList(
                Subscription.builder()
                        .userId(testUserId1)
                        .token("web_push_token_" + UUID.randomUUID())
                        .platform(Subscription.Platform.WEB)
                        .active(true)
                        .build(),
                
                Subscription.builder()
                        .userId(testUserId1)
                        .token("ios_device_token_" + UUID.randomUUID())
                        .platform(Subscription.Platform.IOS)
                        .active(true)
                        .build(),
                
                Subscription.builder()
                        .userId(testUserId2)
                        .token("android_device_token_" + UUID.randomUUID())
                        .platform(Subscription.Platform.ANDROID)
                        .active(true)
                        .build()
        );
        
        subscriptionRepository.saveAll(subscriptions);
        log.info("Seeded {} subscriptions", subscriptions.size());
    }
}
