package com.travel.payment.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.payment.application.usecase.ProcessPaymentUseCase;
import com.travel.payment.application.usecase.RefundPaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Kafka consumer participating in the booking saga.
 *
 * Listens to:
 *   inventory.reservation-confirmed → charge the customer
 *   booking.booking-cancelled       → refund if payment completed
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSagaConsumer {

    private final ProcessPaymentUseCase processUseCase;
    private final RefundPaymentUseCase  refundUseCase;
    private final ObjectMapper          objectMapper;

    @KafkaListener(
        topics  = KafkaTopics.INVENTORY_RESERVATION_CONFIRMED,
        groupId = "payment-service-group"
    )
    public void onInventoryReserved(@Payload String payload,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment ack) {
        handle(payload, topic, ack, node -> {
            String     bookingId = node.get("bookingId").asText();
            String     userId    = node.get("userId").asText();
            BigDecimal amount    = new BigDecimal(
                node.get("totalAmount").get("amount").asText());
            String     currency  = node.get("totalAmount").get("currency").asText();

            processUseCase.execute(bookingId, userId, amount, currency);
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CANCELLED,
        groupId = "payment-service-group"
    )
    public void onBookingCancelled(@Payload String payload,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment ack) {
        handle(payload, topic, ack, node -> {
            String bookingId = node.get("bookingId").asText();
            // Only refund if a completed payment exists for this booking
            refundUseCase.executeByBookingIfCompleted(bookingId);
        });
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private void handle(String payload, String topic,
                        Acknowledgment ack, ConsumerStep step) {
        try {
            log.debug("Payment event from {}: {}", topic, payload);
            step.execute(objectMapper.readTree(payload));
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Payment consumer error for topic {}: {}", topic, ex.getMessage(), ex);
            // Do NOT ack — message redelivered for retry
        }
    }

    @FunctionalInterface
    interface ConsumerStep {
        void execute(JsonNode node) throws Exception;
    }
}
