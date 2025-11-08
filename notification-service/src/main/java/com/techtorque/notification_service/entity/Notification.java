package com.techtorque.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String notificationId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "related_entity_id")
    private String relatedEntityId;

    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @Column
    private LocalDateTime expiresAt;

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        APPOINTMENT_REMINDER,
        APPOINTMENT_CONFIRMED,
        APPOINTMENT_CANCELLED,
        SERVICE_STARTED,
        SERVICE_IN_PROGRESS,
        SERVICE_COMPLETED,
        PAYMENT_RECEIVED,
        PAYMENT_PENDING,
        INVOICE_GENERATED,
        SYSTEM_ALERT
    }
}
