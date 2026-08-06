package com.travel.user.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.user.application.usecase.RecordTravelHistoryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Projects completed bookings into each user's travel history.
 * Relies on the bookingType/resourceName fields added to
 * BookingCompletedEvent in this day's commit 1.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final RecordTravelHistoryUseCase recordUseCase;
    private final ObjectMapper                objectMapper;

    @KafkaListener(topics = KafkaTopics.BOOKING_COMPLETED, groupId = "user-service-group")
    public void onBookingCompleted(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            String userId       = node.get("userId").asText();
            String bookingId    = node.get("bookingId").asText();
            String bookingType  = node.path("bookingType").asText("UNKNOWN");
            // Falls back to resourceId if resourceName is absent — guards
            // against events published before this day's schema addition
            // still sitting in the topic.
            String resourceName = node.path("resourceName").asText(node.get("resourceId").asText());

            recordUseCase.execute(userId, bookingId, bookingType, resourceName);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to record travel history: {}", ex.getMessage(), ex);
        }
    }
}
