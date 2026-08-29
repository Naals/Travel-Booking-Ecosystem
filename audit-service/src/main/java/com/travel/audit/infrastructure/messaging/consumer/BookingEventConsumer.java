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
public class BookingEventConsumer {

    private final RecordAuditEntryUseCase useCase;
    private final ObjectMapper            objectMapper;

    @KafkaListener(topics = KafkaTopics.BOOKING_CREATED, groupId = "audit-service-group")
    public void onBookingCreated(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingCreated", ack, node -> {
            String bookingId = node.get("bookingId").asText();
            String userId    = node.get("userId").asText();
            String type       = node.get("bookingType").asText();
            String resourceId  = node.get("resourceId").asText();
            useCase.execute(AuditCategory.BOOKING, "BookingCreated", node.get("eventId").asText(),
                bookingId, userId, "Booking created: " + type + " " + resourceId, occurredAt(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_CONFIRMED, groupId = "audit-service-group")
    public void onBookingConfirmed(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingConfirmed", ack, node -> {
            String bookingId = node.get("bookingId").asText();
            String userId    = node.get("userId").asText();
            String paymentId  = node.path("paymentId").asText("");
            useCase.execute(AuditCategory.BOOKING, "BookingConfirmed", node.get("eventId").asText(),
                bookingId, userId, "Booking confirmed: paymentId=" + paymentId, occurredAt(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_CANCELLED, groupId = "audit-service-group")
    public void onBookingCancelled(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingCancelled", ack, node -> {
            String bookingId = node.get("bookingId").asText();
            String userId    = node.get("userId").asText();
            String reason     = node.path("reason").asText("");
            useCase.execute(AuditCategory.BOOKING, "BookingCancelled", node.get("eventId").asText(),
                bookingId, userId, "Booking cancelled: " + reason, occurredAt(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_COMPLETED, groupId = "audit-service-group")
    public void onBookingCompleted(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingCompleted", ack, node -> {
            String bookingId    = node.get("bookingId").asText();
            String userId        = node.get("userId").asText();
            String resourceName   = node.path("resourceName").asText(node.get("resourceId").asText());
            useCase.execute(AuditCategory.BOOKING, "BookingCompleted", node.get("eventId").asText(),
                bookingId, userId, "Booking completed: " + resourceName, occurredAt(node));
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
