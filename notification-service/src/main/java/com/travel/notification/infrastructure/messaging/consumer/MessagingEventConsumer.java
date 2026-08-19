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
 * Consumes messaging-service's MessageSentEvent (Day 17).
 * Uses the same userId+"@placeholder.com" stub recipient pattern as
 * BookingEventConsumer and PaymentEventConsumer (Day 9) — see those
 * classes for why a real email lookup isn't wired in yet.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagingEventConsumer {

    private final NotificationDispatcher dispatcher;
    private final ObjectMapper           objectMapper;

    @KafkaListener(topics = KafkaTopics.MESSAGE_SENT, groupId = "notification-service-group")
    public void onMessageSent(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            String recipientId    = node.get("recipientId").asText();
            String senderId       = node.get("senderId").asText();
            String preview        = node.get("contentPreview").asText();
            String conversationId = node.get("conversationId").asText();

            Notification notification = Notification.email(
                recipientId,
                recipientId + "@placeholder.com",
                NotificationType.NEW_MESSAGE,
                Map.of(
                    "senderId",        senderId,
                    "preview",         preview,
                    "conversationUrl", buildConversationUrl(conversationId),
                    "fullName",        "Traveller"
                ));

            dispatcher.dispatch(notification);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process MessageSent notification: {}", ex.getMessage(), ex);
        }
    }

    private String buildConversationUrl(String conversationId) {
        return "https://app.travelplatform.com/messages/" + conversationId;
    }
}
