package com.techtorque.notification_service.service.impl;

import com.techtorque.notification_service.dto.response.SubscriptionResponse;
import com.techtorque.notification_service.entity.Subscription;
import com.techtorque.notification_service.repository.SubscriptionRepository;
import com.techtorque.notification_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public SubscriptionResponse subscribe(String userId, String token, Subscription.Platform platform) {
        log.info("Subscribing user {} to push notifications on platform: {}", userId, platform);
        
        // Check if subscription already exists
        if (subscriptionRepository.existsByUserIdAndToken(userId, token)) {
            Subscription existing = subscriptionRepository.findByUserIdAndToken(userId, token)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));
            
            if (!existing.getActive()) {
                existing.setActive(true);
                existing.setUpdatedAt(LocalDateTime.now());
                subscriptionRepository.save(existing);
            }
            
            return SubscriptionResponse.builder()
                    .subscriptionId(existing.getSubscriptionId())
                    .message("Already subscribed")
                    .build();
        }
        
        // Create new subscription
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .token(token)
                .platform(platform)
                .active(true)
                .build();
        
        Subscription saved = subscriptionRepository.save(subscription);
        
        return SubscriptionResponse.builder()
                .subscriptionId(saved.getSubscriptionId())
                .message("Successfully subscribed")
                .build();
    }

    @Override
    @Transactional
    public void unsubscribe(String userId, String token) {
        log.info("Unsubscribing user {} from push notifications", userId);
        
        Subscription subscription = subscriptionRepository.findByUserIdAndToken(userId, token)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        subscription.setActive(false);
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubscribed(String userId, String token) {
        return subscriptionRepository.existsByUserIdAndToken(userId, token);
    }
}
