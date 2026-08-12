package com.travel.review.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.review.domain.model.ReviewEligibility;
import com.travel.review.domain.model.ReviewedResourceType;
import com.travel.review.domain.repository.ReviewEligibilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Records a ReviewEligibility entry for each completed booking.
 * Relies on the bookingType/resourceName fields user-service's
 * consumer already needed and got added to BookingCompletedEvent
 * on Day 15.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final ReviewEligibilityRepository repository;
    private final ObjectMapper                objectMapper;

    @KafkaListener(topics = KafkaTopics.BOOKING_COMPLETED, groupId = "review-service-group")
    public void onBookingCompleted(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            String userId        = node.get("userId").asText();
            String bookingId     = node.get("bookingId").asText();
            String bookingType   = node.path("bookingType").asText("PROPERTY");
            String resourceId    = extractReviewResourceId(bookingType, node.get("resourceId").asText());

            repository.recordEligibility(ReviewEligibility.of(
                bookingId, userId, resourceId,
                ReviewedResourceType.valueOf(bookingType), Instant.now()));

            ack.acknowledge();
            log.info("Review eligibility recorded: booking={} resource={}", bookingId, resourceId);
        } catch (Exception ex) {
            log.error("Failed to record review eligibility: {}", ex.getMessage(), ex);
        }
    }

    /**
     * PROPERTY: bare propertyId, used as-is.
     * HOTEL:    "<hotelId>:<roomType>"  (ADR-006) — stripped to hotelId;
     *           a review is about the hotel, not one room type.
     * FLIGHT:   "<flightId>:<seatClass>" (ADR-006) — stripped to flightId;
     *           a review is about that specific flight, not the cabin class.
     * VEHICLE:  "<locationCode>:<category>" (ADR-006) — kept AS-IS,
     *           unlike the other three. booking-service's Booking
     *           aggregate never learns which individual vehicleId the
     *           saga assigned (see vehicle-service's VehicleReservedEvent,
     *           Day 13 — the assigned vehicleId is known to
     *           vehicle-service but never threaded back into the
     *           booking), so there is no specific vehicle identity to
     *           review even in principle. Reviewing "SUV rentals at
     *           IST" as a category-at-location experience is both
     *           what the data actually supports and arguably more
     *           useful than a single license plate would be.
     */
    private String extractReviewResourceId(String bookingType, String rawResourceId) {
        return "VEHICLE".equals(bookingType) ? rawResourceId : rawResourceId.split(":")[0];
    }
}
