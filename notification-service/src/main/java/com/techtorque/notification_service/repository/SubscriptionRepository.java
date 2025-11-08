package com.techtorque.notification_service.repository;

import com.techtorque.notification_service.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    List<Subscription> findByUserIdAndActiveTrue(String userId);

    Optional<Subscription> findByUserIdAndToken(String userId, String token);

    Optional<Subscription> findByToken(String token);

    boolean existsByUserIdAndToken(String userId, String token);

    List<Subscription> findByActiveTrueOrderByCreatedAtDesc();
}
