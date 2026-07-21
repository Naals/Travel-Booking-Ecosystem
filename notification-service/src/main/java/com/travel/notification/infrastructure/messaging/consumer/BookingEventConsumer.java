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
 * Consumes booking-service events.
 *
 * BookingConfirmed  → confirmation email
 * BookingCancelled  → cancellation email
 * BookingCompleted  → review request email
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final NotificationDispatcher dispatcher;
    private final ObjectMapper           objectMapper;

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CONFIRMED,
        groupId = "notification-service-group"
    )
    public void onBookingConfirmed(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingConfirmed", ack, node -> {
            String bookingId   = node.get("bookingId").asText();
            String userId      = node.get("userId").asText();
            String totalAmount = node.get("totalAmount").get("amount").asText();
            String currency    = node.get("totalAmount").get("currency").asText();

            // In production: fetch email and fullName from user-service
            // or embed them in the Kafka event (denormalization).
            // Here we use userId as a placeholder recipient for the stub.
            Notification notification = Notification.email(
                userId,
                userId + "@placeholder.com", // replaced by user-service lookup
                NotificationType.BOOKING_CONFIRMED,
                Map.of(
                    "bookingId",   bookingId,
                    "totalAmount", totalAmount,
                    "currency",    currency,
                    "fullName",    "Traveller"
                ));

            dispatcher.dispatch(notification);
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CANCELLED,
        groupId = "notification-service-group"
    )
    public void onBookingCancelled(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingCancelled", ack, node -> {
            String bookingId = node.get("bookingId").asText();
            String userId    = node.get("userId").asText();
            String reason    = node.path("reason").asText("");

            Notification notification = Notification.email(
                userId,
                userId + "@placeholder.com",
                NotificationType.BOOKING_CANCELLED,
                Map.of(
                    "bookingId", bookingId,
                    "reason",    reason,
                    "fullName",  "Traveller"
                ));

            dispatcher.dispatch(notification);
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_COMPLETED,
        groupId = "notification-service-group"
    )
    public void onBookingCompleted(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingCompleted", ack, node -> {
            String bookingId  = node.get("bookingId").asText();
            String userId     = node.get("userId").asText();
            String resourceId = node.get("resourceId").asText();

            Notification notification = Notification.email(
                userId,
                userId + "@placeholder.com",
                NotificationType.REVIEW_REQUEST,
                Map.of(
                    "bookingId",    bookingId,
                    "resourceName", resourceId,
                    "fullName",     "Traveller",
                    "reviewUrl",    buildReviewUrl(bookingId)
                ));

            dispatcher.dispatch(notification);
        });
    }

    private String buildReviewUrl(String bookingId) {
        return "https://app.travelplatform.com/reviews/new?bookingId=" + bookingId;
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
