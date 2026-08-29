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
public class FraudEventConsumer {

    private final RecordAuditEntryUseCase useCase;
    private final ObjectMapper            objectMapper;

    @KafkaListener(topics = KafkaTopics.FRAUD_ALERT_RAISED, groupId = "audit-service-group")
    public void onFraudAlertRaised(@Payload String payload, Acknowledgment ack) {
        handle(payload, "FraudAlertRaised", ack, node -> {
            String userId   = node.get("userId").asText();
            String ruleName = node.get("ruleName").asText();
            String reason     = node.get("reason").asText();
            useCase.execute(AuditCategory.FRAUD, "FraudAlertRaised", node.get("eventId").asText(),
                userId, userId, "Fraud alert raised: " + ruleName + " — " + reason, occurredAt(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.FRAUD_RISK_FLAG_CLEARED, groupId = "audit-service-group")
    public void onRiskFlagCleared(@Payload String payload, Acknowledgment ack) {
        handle(payload, "RiskFlagCleared", ack, node -> {
            String userId  = node.get("userId").asText();
            String staffId  = node.get("clearedByStaffId").asText();
            useCase.execute(AuditCategory.FRAUD, "RiskFlagCleared", node.get("eventId").asText(),
                userId, userId, "Risk flag cleared by staff " + staffId, occurredAt(node));
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
