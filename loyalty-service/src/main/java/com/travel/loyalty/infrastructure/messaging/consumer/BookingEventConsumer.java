package com.travel.loyalty.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.loyalty.application.usecase.AwardPointsForCompletedBookingUseCase;
import com.travel.loyalty.application.usecase.RecordSpendUseCase;
import com.travel.loyalty.domain.repository.SpendRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Three narrow handlers, one per stage of a booking's lifecycle
 * relevant to points-earning — see SpendRecord's class Javadoc for
 * why three, not two.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final RecordSpendUseCase                   recordSpendUseCase;
    private final AwardPointsForCompletedBookingUseCase awardPointsUseCase;
    private final SpendRecordRepository                 spendRecordRepository;
    private final ObjectMapper                          objectMapper;

    @KafkaListener(topics = KafkaTopics.BOOKING_CONFIRMED, groupId = "loyalty-service-group")
    public void onBookingConfirmed(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            String     bookingId = node.get("bookingId").asText();
            String     userId    = node.get("userId").asText();
            BigDecimal amount    = new BigDecimal(node.get("totalAmount").get("amount").asText());
            String     currency  = node.get("totalAmount").get("currency").asText();

            recordSpendUseCase.execute(bookingId, userId, amount, currency);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to record spend from BookingConfirmed event: {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_COMPLETED, groupId = "loyalty-service-group")
    public void onBookingCompleted(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node      = objectMapper.readTree(payload);
            String    bookingId = node.get("bookingId").asText();

            awardPointsUseCase.execute(bookingId);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to award points from BookingCompleted event: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Voids a pending spend record when a CONFIRMED booking is
     * cancelled before check-in, so it can never later be spuriously
     * consumed if BookingCompleted somehow still arrived. If no record
     * exists (booking was never PROPERTY/HOTEL/FLIGHT/VEHICLE type, or
     * was cancelled before reaching CONFIRMED), this is a silent no-op.
     */
    @KafkaListener(topics = KafkaTopics.BOOKING_CANCELLED, groupId = "loyalty-service-group")
    public void onBookingCancelled(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node      = objectMapper.readTree(payload);
            String    bookingId = node.get("bookingId").asText();

            spendRecordRepository.voidIfExists(bookingId);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to void spend record from BookingCancelled event: {}", ex.getMessage(), ex);
        }
    }
}
