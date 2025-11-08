package com.techtorque.notification_service.service;

import com.techtorque.notification_service.dto.response.SubscriptionResponse;
import com.techtorque.notification_service.entity.Subscription;

public interface SubscriptionService {

    SubscriptionResponse subscribe(String userId, String token, Subscription.Platform platform);

    void unsubscribe(String userId, String token);

    boolean isSubscribed(String userId, String token);
}
