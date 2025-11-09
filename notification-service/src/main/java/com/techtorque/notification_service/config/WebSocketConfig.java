package com.techtorque.notification_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time notifications
 * Uses STOMP (Simple Text Oriented Messaging Protocol) over WebSocket
 *
 * Industry standard configuration following Spring Boot best practices
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker for handling messages
     * - /topic: for broadcasting to multiple subscribers (pub-sub pattern)
     * - /queue: for point-to-point messaging (user-specific)
     * - /app: prefix for messages from client to server
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker for pub-sub
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages from client to server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints that clients will connect to
     * - /ws/notifications: Main WebSocket endpoint
     * - SockJS fallback for browsers that don't support WebSocket
     * - CORS allowed for development (configure properly for production)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")  // Allow all origins for development
                .withSockJS(); // Enable SockJS fallback for older browsers

        // Plain WebSocket endpoint without SockJS (for modern clients)
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*");  // Allow all origins for development
    }
}
