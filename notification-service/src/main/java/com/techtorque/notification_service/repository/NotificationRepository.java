package com.techtorque.notification_service.repository;

import com.techtorque.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(String userId);

    List<Notification> findByUserIdAndReadAndDeletedFalseOrderByCreatedAtDesc(String userId, Boolean read);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.deleted = false AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.deleted = false AND n.read = false")
    Long countUnreadByUserId(@Param("userId") String userId);

    @Query("SELECT n FROM Notification n WHERE n.expiresAt < :now AND n.deleted = false")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

    List<Notification> findByRelatedEntityIdAndRelatedEntityType(String entityId, String entityType);
}
