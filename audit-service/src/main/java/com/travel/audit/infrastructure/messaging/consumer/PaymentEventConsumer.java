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
public class PaymentEventConsumer {

    private final RecordAuditEntryUseCase useCase;
    private final ObjectMapper            objectMapper;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "audit-service-group")
    public void onPaymentCompleted(@Payload String payload, Acknowledgment ack) {
        handle(payload, "PaymentCompleted", ack, node -> {
            String paymentId = node.get("paymentId").asText();
            String userId    = node.get("userId").asText();
            String amount     = node.get("amount").get("amount").asText();
            String currency    = node.get("amount").get("currency").asText();
            useCase.execute(AuditCategory.PAYMENT, "PaymentCompleted", node.get("eventId").asText(),
                paymentId, userId, "Payment completed: " + amount + " " + currency, occurredAt(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "audit-service-group")
    public void onPaymentFailed(@Payload String payload, Acknowledgment ack) {
        handle(payload, "PaymentFailed", ack, node -> {
            String paymentId = node.get("paymentId").asText();
            String userId    = node.get("userId").asText();
            String reason     = node.path("reason").asText("");
            useCase.execute(AuditCategory.PAYMENT, "PaymentFailed", node.get("eventId").asText(),
                paymentId, userId, "Payment failed: " + reason, occurredAt(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.REFUND_COMPLETED, groupId = "audit-service-group")
    public void onRefundCompleted(@Payload String payload, Acknowledgment ack) {
        handle(payload, "RefundCompleted", ack, node -> {
            String paymentId = node.get("paymentId").asText();
            String userId    = node.get("userId").asText();
            String amount     = node.get("amount").get("amount").asText();
            String currency    = node.get("amount").get("currency").asText();
            String refundId      = node.get("refundId").asText();
            String summary = "Refund completed: " + amount + " " + currency + " (refundId=" + refundId + ")";
            useCase.execute(AuditCategory.PAYMENT, "RefundCompleted", node.get("eventId").asText(),
                paymentId, userId, summary, occurredAt(node));
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
