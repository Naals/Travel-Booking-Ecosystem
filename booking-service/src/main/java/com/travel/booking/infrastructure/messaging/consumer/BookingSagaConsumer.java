package com.travel.booking.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.booking.application.saga.BookingSaga;
import com.travel.common.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer driving the booking saga state machine.
 *
 * Uses manual acknowledgment — message is only committed after the saga
 * transition succeeds and the aggregate is saved. If processing fails,
 * the message is not acknowledged and will be redelivered (at-least-once).
 *
 * Idempotency: the aggregate state machine enforces this — calling
 * markInventoryReserved() on an already-INVENTORY_RESERVED booking
 * throws BusinessRuleViolationException which is caught and logged here,
 * and the message is acknowledged to prevent infinite retry loops.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingSagaConsumer {

    private final BookingSaga  saga;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics  = "inventory.reservation-confirmed",
        groupId = "booking-saga-group"
    )
    public void onInventoryReserved(@Payload String payload,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment ack) {
        handle(payload, topic, ack, node ->
            saga.onInventoryReserved(node.get("bookingId").asText()));
    }

    @KafkaListener(
        topics  = "inventory.reservation-failed",
        groupId = "booking-saga-group"
    )
    public void onInventoryFailed(@Payload String payload,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  Acknowledgment ack) {
        handle(payload, topic, ack, node ->
            saga.onInventoryUnavailable(
                node.get("bookingId").asText(),
                node.path("reason").asText("Resource unavailable")));
    }

    @KafkaListener(
        topics  = KafkaTopics.PAYMENT_COMPLETED,
        groupId = "booking-saga-group"
    )
    public void onPaymentCompleted(@Payload String payload,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment ack) {
        handle(payload, topic, ack, node ->
            saga.onPaymentCompleted(node.get("bookingId").asText()));
    }

    @KafkaListener(
        topics  = KafkaTopics.PAYMENT_FAILED,
        groupId = "booking-saga-group"
    )
    public void onPaymentFailed(@Payload String payload,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                Acknowledgment ack) {
        handle(payload, topic, ack, node ->
            saga.onPaymentFailed(
                node.get("bookingId").asText(),
                node.path("reason").asText("Payment declined")));
    }

    @KafkaListener(
        topics  = "inventory.reservation-released",
        groupId = "booking-saga-group"
    )
    public void onInventoryReleased(@Payload String payload,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment ack) {
        handle(payload, topic, ack, node ->
            saga.onInventoryReleased(node.get("bookingId").asText()));
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private void handle(String payload, String topic,
                        Acknowledgment ack, SagaStep step) {
        try {
            log.debug("SAGA event from {}: {}", topic, payload);
            step.execute(objectMapper.readTree(payload));
            ack.acknowledge();
        } catch (com.travel.common.exception.BusinessRuleViolationException ex) {
            // Idempotency guard — transition already applied, safe to ack
            log.warn("SAGA idempotency guard triggered for topic {}: {}", topic, ex.getMessage());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("SAGA processing failed for topic {}: {}", topic, ex.getMessage(), ex);
            // Do NOT ack — message will be redelivered
        }
    }

    @FunctionalInterface
    interface SagaStep {
        void execute(JsonNode node) throws Exception;
    }
}
