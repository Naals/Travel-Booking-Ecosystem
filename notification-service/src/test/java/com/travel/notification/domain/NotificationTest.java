package com.travel.notification.domain;

import com.travel.notification.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Notification domain model")
class NotificationTest {

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test @DisplayName("email() creates EMAIL channel notification")
        void emailFactory() {
            Notification n = Notification.email(
                "user-1", "user@example.com",
                NotificationType.WELCOME,
                Map.of("fullName", "John Doe"));

            assertThat(n.getChannel()).isEqualTo(NotificationChannel.EMAIL);
            assertThat(n.getType()).isEqualTo(NotificationType.WELCOME);
            assertThat(n.getRecipient()).isEqualTo("user@example.com");
            assertThat(n.getUserId()).isEqualTo("user-1");
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test @DisplayName("sms() creates SMS channel notification")
        void smsFactory() {
            Notification n = Notification.sms(
                "user-1", "+1234567890",
                NotificationType.BOOKING_CONFIRMED,
                Map.of("bookingId", "bk-123"));

            assertThat(n.getChannel()).isEqualTo(NotificationChannel.SMS);
            assertThat(n.getRecipient()).isEqualTo("+1234567890");
        }

        @Test @DisplayName("push() creates PUSH channel notification")
        void pushFactory() {
            Notification n = Notification.push(
                "user-1", "fcm-device-token",
                NotificationType.LOYALTY_POINTS_EARNED,
                Map.of("points", "100"));

            assertThat(n.getChannel()).isEqualTo(NotificationChannel.PUSH);
            assertThat(n.getRecipient()).isEqualTo("fcm-device-token");
        }

        @Test @DisplayName("each notification gets a unique ID")
        void uniqueIds() {
            Notification a = Notification.email("u1", "a@a.com",
                NotificationType.WELCOME, Map.of());
            Notification b = Notification.email("u2", "b@b.com",
                NotificationType.WELCOME, Map.of());
            assertThat(a.getId()).isNotEqualTo(b.getId());
        }
    }

    @Nested
    @DisplayName("Status transitions")
    class StatusTransitions {

        @Test @DisplayName("markSent transitions to SENT")
        void markSent() {
            Notification n = Notification.email("u1", "a@a.com",
                NotificationType.WELCOME, Map.of());
            n.markSent();
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(n.getErrorMessage()).isNull();
        }

        @Test @DisplayName("markFailed stores error message and transitions to FAILED")
        void markFailed() {
            Notification n = Notification.email("u1", "a@a.com",
                NotificationType.WELCOME, Map.of());
            n.markFailed("SMTP connection refused");
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(n.getErrorMessage()).isEqualTo("SMTP connection refused");
        }
    }

    @Nested
    @DisplayName("Template variables")
    class TemplateVariables {

        @Test @DisplayName("templateVariables are stored and accessible")
        void templateVariables() {
            Map<String, Object> vars = Map.of(
                "fullName",    "John Doe",
                "bookingId",   "bk-123",
                "totalAmount", "299.99");

            Notification n = Notification.email("u1", "a@a.com",
                NotificationType.BOOKING_CONFIRMED, vars);

            assertThat(n.getTemplateVariables())
                .containsEntry("fullName", "John Doe")
                .containsEntry("bookingId", "bk-123");
        }
    }
}
