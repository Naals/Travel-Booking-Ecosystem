package com.travel.audit.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.audit.application.usecase.RecordAuditEntryUseCase;
import com.travel.audit.domain.model.AuditCategory;
import com.travel.common.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityEventConsumer {

    private final RecordAuditEntryUseCase useCase;
    private final ObjectMapper            objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "audit-service-group")
    public void onUserRegistered(@Payload String payload, Acknowledgment ack) {
        handle(payload, "UserRegistered", ack, node -> {
            String userId = node.get("userId").asText();
            String email  = node.get("email").asText();
            useCase.execute(AuditCategory.IDENTITY, "UserRegistered", node.get("eventId").asText(),
                userId, userId, "User registered: " + email, occurredAt(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.USER_DEACTIVATED, groupId = "audit-service-group")
    public void onUserDeactivated(@Payload String payload, Acknowledgment ack) {
        handle(payload, "UserDeactivated", ack, node -> {
            String userId = node.get("userId").asText();
            String email  = node.get("email").asText();
            String reason = node.path("reason").asText("");
            String summary = "User deactivated: " + email + (reason.isBlank() ? "" : " (" + reason + ")");
            useCase.execute(AuditCategory.IDENTITY, "UserDeactivated", node.get("eventId").asText(),
                userId, userId, summary, occurredAt(node));
        });
    }

    private Instant occurredAt(JsonNode node) { return Instant.parse(node.get("occurredOn").asText()); }

    private void handle(String payload, String eventType, Acknowledgment ack, ConsumerStep step) {
        try {
            step.execute(objectMapper.readTree(payload));
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to record audit entry for {}: {}", eventType, ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    interface ConsumerStep { void execute(JsonNode node) throws Exception; }
}
