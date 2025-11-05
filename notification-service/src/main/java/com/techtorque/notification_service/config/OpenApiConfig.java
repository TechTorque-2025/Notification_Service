package com.techtorque.notification_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Notification Service
 * 
 * Configures API documentation with:
 * - Service information and contact details
 * - Security schemes (Bearer JWT via API Gateway)
 * - Server URLs for different environments
 * 
 * Access Swagger UI at: http://localhost:8088/swagger-ui/index.html
 * Access API docs JSON at: http://localhost:8088/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechTorque Notification Service API")
                        .version("1.0.0")
                        .description(
                            "REST API for notification and email management. " +
                            "This service handles user notifications, email alerts, and subscription management.\n\n" +
                            "**Key Features:**\n" +
                            "- Send and receive notifications\n" +
                            "- Email notification delivery\n" +
                            "- Subscription management for notification preferences\n" +
                            "- Mark notifications as read/unread\n" +
                            "- Query notification history\n\n" +
                            "**Authentication:**\n" +
                            "All endpoints require JWT authentication via the API Gateway. " +
                            "The gateway validates the JWT and injects user context via headers."
                        )
                        .contact(new Contact()
                                .name("TechTorque Development Team")
                                .email("dev@techtorque.com")
                                .url("https://techtorque.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://techtorque.com/license"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8088")
                                .description("Local development server"),
                        new Server()
                                .url("http://localhost:8080/api/v1")
                                .description("Local API Gateway"),
                        new Server()
                                .url("https://api.techtorque.com/v1")
                                .description("Production API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from authentication service (validated by API Gateway)")
                        )
                );
    }
}
