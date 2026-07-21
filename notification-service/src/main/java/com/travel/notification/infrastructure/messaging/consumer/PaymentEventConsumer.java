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
 * Consumes payment-service events.
 *
 * PaymentFailed    → failure email (booking inventory already released)
 * RefundCompleted  → refund confirmation email
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationDispatcher dispatcher;
    private final ObjectMapper           objectMapper;

    @KafkaListener(
        topics  = KafkaTopics.PAYMENT_FAILED,
        groupId = "notification-service-group"
    )
    public void onPaymentFailed(@Payload String payload, Acknowledgment ack) {
        handle(payload, "PaymentFailed", ack, node -> {
            String bookingId = node.get("bookingId").asText();
            String userId    = node.get("userId").asText();

            Notification notification = Notification.email(
                userId,
                userId + "@placeholder.com",
                NotificationType.PAYMENT_FAILED,
                Map.of(
                    "bookingId", bookingId,
                    "fullName",  "Traveller",
                    "retryUrl",  "https://app.travelplatform.com/bookings/new"
                ));

            dispatcher.dispatch(notification);
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.REFUND_COMPLETED,
        groupId = "notification-service-group"
    )
    public void onRefundCompleted(@Payload String payload, Acknowledgment ack) {
        handle(payload, "RefundCompleted", ack, node -> {
            String bookingId = node.get("bookingId").asText();
            String userId    = node.get("userId").asText();
            String amount    = node.get("amount").get("amount").asText();
            String currency  = node.get("amount").get("currency").asText();
            String refundId  = node.get("refundId").asText();

            Notification notification = Notification.email(
                userId,
                userId + "@placeholder.com",
                NotificationType.PAYMENT_REFUNDED,
                Map.of(
                    "bookingId", bookingId,
                    "fullName",  "Traveller",
                    "amount",    amount,
                    "currency",  currency,
                    "refundId",  refundId
                ));

            dispatcher.dispatch(notification);
        });
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
