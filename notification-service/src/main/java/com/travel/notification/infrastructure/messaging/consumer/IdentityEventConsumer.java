package com.travel.notification.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.notification.application.service.NotificationDispatcher;
import com.travel.notification.domain.model.Notification;
import com.travel.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes identity-service events.
 * UserRegistered → welcome email + email verification link.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityEventConsumer {

    private final NotificationDispatcher dispatcher;
    private final ObjectMapper           objectMapper;

    @KafkaListener(
        topics  = KafkaTopics.USER_REGISTERED,
        groupId = "notification-service-group"
    )
    public void onUserRegistered(@Payload String payload, Acknowledgment ack) {
        handle(payload, "UserRegistered", ack, node -> {
            String userId   = node.get("userId").asText();
            String email    = node.get("email").asText();
            String fullName = node.get("fullName").asText();

            Notification notification = Notification.email(
                userId, email,
                NotificationType.WELCOME,
                Map.of(
                    "fullName",        fullName,
                    "verificationUrl", buildVerificationUrl(userId)
                ));

            dispatcher.dispatch(notification);
        });
    }

    private String buildVerificationUrl(String userId) {
        return "https://app.travelplatform.com/verify-email?userId=" + userId;
    }

    private void handle(String payload, String eventType,
                        Acknowledgment ack, ConsumerStep step) {
        try {
            step.execute(objectMapper.readTree(payload));
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed processing {} notification: {}", eventType, ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    interface ConsumerStep {
        void execute(JsonNode node) throws Exception;
    }
}
