package com.travel.fraud.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.fraud.application.usecase.RecordBookingCompletedUseCase;
import com.travel.fraud.application.usecase.RecordBookingCreatedUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** No bookingType filtering — velocity is evaluated the same regardless of resource type. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final RecordBookingCreatedUseCase   recordCreatedUseCase;
    private final RecordBookingCompletedUseCase recordCompletedUseCase;
    private final ObjectMapper                   objectMapper;

    @KafkaListener(topics = KafkaTopics.BOOKING_CREATED, groupId = "fraud-service-group")
    public void onBookingCreated(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node   = objectMapper.readTree(payload);
            String    userId = node.get("userId").asText();
            recordCreatedUseCase.execute(userId, Instant.now());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to record BookingCreated for fraud evaluation: {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_COMPLETED, groupId = "fraud-service-group")
    public void onBookingCompleted(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node   = objectMapper.readTree(payload);
            String    userId = node.get("userId").asText();
            recordCompletedUseCase.execute(userId);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to record BookingCompleted for fraud profile: {}", ex.getMessage(), ex);
        }
    }
}
