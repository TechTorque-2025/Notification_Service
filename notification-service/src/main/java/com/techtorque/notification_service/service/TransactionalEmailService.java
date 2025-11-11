package com.techtorque.notification_service.service;

import com.techtorque.notification.grpc.DeliveryStatus;
import com.techtorque.notification.grpc.EmailType;
import com.techtorque.notification_service.config.NotificationProperties;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Handles transactional email composition and delivery triggered via gRPC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionalEmailService {

    private final JavaMailSender mailSender;
    private final NotificationProperties notificationProperties;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    public DeliveryResult sendTransactionalEmail(String to,
                                                 String username,
                                                 EmailType type,
                                                 Map<String, String> variables) {
        String fromAddress = resolveFromAddress();
        EmailTemplate template = buildTemplate(username, type, variables);
        String messageId = UUID.randomUUID().toString();

        if (!emailEnabled) {
            log.info("Email delivery disabled. Skipping send for {} ({})", to, type);
            return new DeliveryResult(messageId, DeliveryStatus.DELIVERY_STATUS_ACCEPTED,
                    "Email delivery disabled by configuration");
        }

        try {
            if (mailSender == null) {
                log.warn("Mail sender not configured. Unable to deliver {} email to {}", type, to);
                return new DeliveryResult(messageId, DeliveryStatus.DELIVERY_STATUS_REJECTED,
                        "Mail sender not configured");
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(template.subject());
            message.setText(template.body());
            mailSender.send(message);
            log.info("Transactional email {} queued to {} at {}", type, to, OffsetDateTime.now());
            return new DeliveryResult(messageId, DeliveryStatus.DELIVERY_STATUS_ACCEPTED, "Email sent");
        } catch (Exception ex) {
            log.error("Failed to send {} email to {}: {}", type, to, ex.getMessage(), ex);
            return new DeliveryResult(messageId, DeliveryStatus.DELIVERY_STATUS_REJECTED, ex.getMessage());
        }
    }

    private String resolveFromAddress() {
        String configured = notificationProperties.getEmail() != null
                ? notificationProperties.getEmail().getFrom()
                : null;
        return StringUtils.hasText(configured) ? configured : "noreply@techtorque.com";
    }

    private EmailTemplate buildTemplate(String username, EmailType type, Map<String, String> variables) {
        String safeUsername = StringUtils.hasText(username) ? username : "there";
        return switch (type) {
            case EMAIL_TYPE_PASSWORD_RESET -> passwordResetTemplate(safeUsername, variables);
            case EMAIL_TYPE_WELCOME -> welcomeTemplate(safeUsername, variables);
            case EMAIL_TYPE_VERIFICATION, EMAIL_TYPE_UNSPECIFIED -> verificationTemplate(safeUsername, variables);
            default -> verificationTemplate(safeUsername, variables);
        };
    }

    private EmailTemplate verificationTemplate(String username, Map<String, String> variables) {
        String verificationUrl = variables.getOrDefault("verificationUrl", "");
        String token = variables.getOrDefault("token", "");
        String subject = "TechTorque - Verify Your Email Address";
        StringBuilder body = new StringBuilder()
                .append("Hello ").append(username).append(",\n\n")
                .append("Thank you for registering with TechTorque!\n\n");

        if (StringUtils.hasText(verificationUrl)) {
            body.append("Please click the link below to verify your email address:\n")
                    .append(verificationUrl).append("\n\n");
        } else if (StringUtils.hasText(token)) {
            body.append("Use the following verification token to complete your registration:\n")
                    .append(token).append("\n\n");
        }

        body.append("This link will expire in 24 hours.\n\n")
                .append("If you did not create an account, please ignore this email.\n\n")
                .append("Best regards,\nTechTorque Team");
        return new EmailTemplate(subject, body.toString());
    }

    private EmailTemplate passwordResetTemplate(String username, Map<String, String> variables) {
        String resetUrl = variables.getOrDefault("resetUrl", "");
        String token = variables.getOrDefault("token", "");
        String subject = "TechTorque - Password Reset Request";
        StringBuilder body = new StringBuilder()
                .append("Hello ").append(username).append(",\n\n")
                .append("We received a request to reset your password.\n\n");

        if (StringUtils.hasText(resetUrl)) {
            body.append("Please click the link below to reset your password:\n")
                    .append(resetUrl).append("\n\n");
        } else if (StringUtils.hasText(token)) {
            body.append("Use the following password reset token to update your credentials:\n")
                    .append(token).append("\n\n");
        }

        body.append("This link will expire in 1 hour.\n\n")
                .append("If you did not request a password reset, please ignore this email and your password will remain unchanged.\n\n")
                .append("Best regards,\nTechTorque Team");
        return new EmailTemplate(subject, body.toString());
    }

    private EmailTemplate welcomeTemplate(String username, Map<String, String> variables) {
        String dashboardUrl = variables.getOrDefault("dashboardUrl", "");
        String subject = "Welcome to TechTorque!";
        StringBuilder body = new StringBuilder()
                .append("Hello ").append(username).append(",\n\n")
                .append("Welcome to TechTorque! Your email has been successfully verified.\n\n")
                .append("You can now:\n")
                .append("- Register your vehicles\n")
                .append("- Book service appointments\n")
                .append("- Track service progress\n")
                .append("- Request custom modifications\n\n");

        if (StringUtils.hasText(dashboardUrl)) {
            body.append("Visit ").append(dashboardUrl).append(" to get started.\n\n");
        }

        body.append("Best regards,\nTechTorque Team");
        return new EmailTemplate(subject, body.toString());
    }

    public record DeliveryResult(String messageId, DeliveryStatus status, String detail) {}

    private record EmailTemplate(String subject, String body) {}
}
