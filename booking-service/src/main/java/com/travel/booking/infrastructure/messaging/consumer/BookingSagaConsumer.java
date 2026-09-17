package com.travel.booking.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.booking.application.saga.BookingSaga;
import com.travel.common.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Reworked Day 24 (ADR-016). Two changes from the Day 7 original:
 *
 *   1. A new onPaymentInitiated() listener on
 *      KafkaTopics.PAYMENT_REQUESTED — the saga method existed since
 *      Day 7 but had no listener wired to it at all.
 *
 *   2. handle() no longer catches every exception uniformly. A
 *      caught-and-logged SagaOutOfOrderException would leave a
 *      booking stuck forever with no way to self-correct. Now only
 *      JSON parsing failures (which can never succeed on retry) are
 *      caught here; everything else — including SagaOutOfOrderException
 *      — propagates to the container's error handler (see
 *      KafkaErrorHandlingConfig), which retries the specific record
 *      with backoff and, if genuinely exhausted, routes it to a .dlq
 *      topic instead of silently dropping it or blocking the
 *      partition forever.
 *
 * Also switched from hardcoded topic string literals (as Day 7 wrote
 * them) to the KafkaTopics constants that were declared for these
 * exact topics since Day 3 — a small consistency fix made while
 * already touching this file.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingSagaConsumer {

    private final BookingSaga  saga;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVATION_CONFIRMED, groupId = "booking-saga-group")
    public void onInventoryReserved(String payload, Acknowledgment ack) {
        handle(payload, ack, node -> saga.onInventoryReserved(node.get("bookingId").asText()));
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVATION_FAILED, groupId = "booking-saga-group")
    public void onInventoryFailed(String payload, Acknowledgment ack) {
        handle(payload, ack, node -> saga.onInventoryUnavailable(
            node.get("bookingId").asText(), node.path("reason").asText("Resource unavailable")));
    }

    /** New Day 24 — see class Javadoc. Reuses PAYMENT_REQUESTED (Day 3), the 7th Day-3-seeded constant to finally get a producer AND consumer — see ADR-016. */
    @KafkaListener(topics = KafkaTopics.PAYMENT_REQUESTED, groupId = "booking-saga-group")
    public void onPaymentInitiated(String payload, Acknowledgment ack) {
        handle(payload, ack, node -> saga.onPaymentInitiated(
            node.get("bookingId").asText(), node.get("paymentId").asText()));
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "booking-saga-group")
    public void onPaymentCompleted(String payload, Acknowledgment ack) {
        handle(payload, ack, node -> saga.onPaymentCompleted(node.get("bookingId").asText()));
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "booking-saga-group")
    public void onPaymentFailed(String payload, Acknowledgment ack) {
        handle(payload, ack, node -> saga.onPaymentFailed(
            node.get("bookingId").asText(), node.path("reason").asText("Payment declined")));
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVATION_RELEASED, groupId = "booking-saga-group")
    public void onInventoryReleased(String payload, Acknowledgment ack) {
        handle(payload, ack, node -> saga.onInventoryReleased(node.get("bookingId").asText()));
    }

    private void handle(String payload, Acknowledgment ack, SagaStep step) {
        JsonNode node;
        try {
            node = objectMapper.readTree(payload);
        } catch (Exception ex) {
            // A malformed payload can never parse correctly no matter
            // how many times it's retried — ack now rather than let
            // the error handler burn its retry budget on a message
            // that can never succeed.
            log.error("Malformed saga event payload, dropping: {}", ex.getMessage(), ex);
            ack.acknowledge();
            return;
        }

        step.execute(node); // SagaOutOfOrderException, if thrown, propagates deliberately
        ack.acknowledge();
    }

    @FunctionalInterface
    interface SagaStep {
        void execute(JsonNode node);
    }
}
