package com.techtorque.notification_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds properties under the prefix `notification` (e.g. notification.email.from).
 */
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    /** maps to notification.email.from */
    private Email email = new Email();

    /** maps to notification.retention.days */
    private Retention retention = new Retention();

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Retention getRetention() {
        return retention;
    }

    public void setRetention(Retention retention) {
        this.retention = retention;
    }

    public static class Email {
        /** maps to notification.email.from */
        private String from;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        @Override
        public String toString() {
            return "Email{from='" + from + "'}";
        }
    }

    public static class Retention {
        /** maps to notification.retention.days */
        private int days = 30;

        public int getDays() {
            return days;
        }

        public void setDays(int days) {
            this.days = days;
        }

        @Override
        public String toString() {
            return "Retention{days=" + days + "}";
        }
    }

    @Override
    public String toString() {
        return "NotificationProperties{" + "email=" + email + ", retention=" + retention + '}';
    }
}
