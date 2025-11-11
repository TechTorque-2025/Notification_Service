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

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;

    // Consistent user IDs matching Auth Service seed data
    // These usernames are forwarded via X-User-Subject header from the API Gateway
    private static final String CUSTOMER_1_ID = "customer";
    private static final String CUSTOMER_2_ID = "testuser";
    private static final String EMPLOYEE_1_ID = "employee";

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
        log.info("Seeding notifications for consistent test users...");
        
        // Use consistent user IDs that match Auth Service seed data
        // No more random UUIDs - ensures cross-service data integrity
        List<Notification> notifications = Arrays.asList(
                Notification.builder()
                        .userId(CUSTOMER_1_ID)
                        .type(Notification.NotificationType.APPOINTMENT_CONFIRMED)
                        .message("Your appointment has been confirmed")
                        .details("Appointment scheduled for tomorrow at 10:00 AM")
                        .read(false)
                        .deleted(false)
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build(),
                
                Notification.builder()
                        .userId(CUSTOMER_1_ID)
                        .type(Notification.NotificationType.SERVICE_COMPLETED)
                        .message("Your service has been completed")
                        .details("Oil change and tire rotation completed successfully")
                        .read(true)
                        .deleted(false)
                        .readAt(LocalDateTime.now().minusHours(2))
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build(),
                
                Notification.builder()
                        .userId(CUSTOMER_1_ID)
                        .type(Notification.NotificationType.INVOICE_GENERATED)
                        .message("Invoice generated for your service")
                        .details("Total amount: $150.00. Payment due in 7 days.")
                        .read(false)
                        .deleted(false)
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build(),
                
                Notification.builder()
                        .userId(CUSTOMER_2_ID)
                        .type(Notification.NotificationType.APPOINTMENT_REMINDER)
                        .message("Appointment reminder")
                        .details("Your appointment is in 1 hour")
                        .read(false)
                        .deleted(false)
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .build(),
                
                Notification.builder()
                        .userId(CUSTOMER_2_ID)
                        .type(Notification.NotificationType.PAYMENT_RECEIVED)
                        .message("Payment received")
                        .details("We've received your payment of $200.00")
                        .read(true)
                        .deleted(false)
                        .readAt(LocalDateTime.now().minusDays(1))
                        .expiresAt(LocalDateTime.now().plusDays(30))
                        .build(),
                
                Notification.builder()
                        .userId(EMPLOYEE_1_ID)
                        .type(Notification.NotificationType.APPOINTMENT_CONFIRMED)
                        .message("New appointment assigned to you")
                        .details("Customer appointment scheduled for today at 2:00 PM")
                        .read(false)
                        .deleted(false)
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build()
        );
        
        notificationRepository.saveAll(notifications);
        log.info("Seeded {} notifications for users: {}, {}, {}", 
                notifications.size(), CUSTOMER_1_ID, CUSTOMER_2_ID, EMPLOYEE_1_ID);
    }

    private void seedSubscriptions() {
        log.info("Seeding subscriptions for consistent test users...");
        
        // Use consistent user IDs and predictable tokens for testing
        // No more random UUIDs - ensures reproducible test data
        List<Subscription> subscriptions = Arrays.asList(
                Subscription.builder()
                        .userId(CUSTOMER_1_ID)
                        .token("web_push_token_customer_browser")
                        .platform(Subscription.Platform.WEB)
                        .active(true)
                        .build(),
                
                Subscription.builder()
                        .userId(CUSTOMER_1_ID)
                        .token("ios_device_token_customer_iphone")
                        .platform(Subscription.Platform.IOS)
                        .active(true)
                        .build(),
                
                Subscription.builder()
                        .userId(CUSTOMER_2_ID)
                        .token("android_device_token_testuser_phone")
                        .platform(Subscription.Platform.ANDROID)
                        .active(true)
                        .build(),
                
                Subscription.builder()
                        .userId(EMPLOYEE_1_ID)
                        .token("web_push_token_employee_browser")
                        .platform(Subscription.Platform.WEB)
                        .active(true)
                        .build()
        );
        
        subscriptionRepository.saveAll(subscriptions);
        log.info("Seeded {} subscriptions for users: {}, {}, {}", 
                subscriptions.size(), CUSTOMER_1_ID, CUSTOMER_2_ID, EMPLOYEE_1_ID);
    }
}
