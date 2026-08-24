package com.travel.recommendation.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.recommendation.application.usecase.RecordAffinitySignalUseCase;
import com.travel.recommendation.application.usecase.RecordPopularitySignalUseCase;
import com.travel.recommendation.domain.model.AffinitySignalType;
import com.travel.recommendation.domain.repository.DestinationLookupRepository;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The first consumer of user-service's SavedLocationAddedEvent (Day
 * 15), published for 3+ days with the explicit note "intended for a
 * future recommendation-service to build destination-affinity signals
 * from." This is that future arriving.
 *
 * onBookingCompleted resolves resourceId to a destination via
 * DestinationLookup, using the same per-bookingType resourceId parsing
 * convention review-service established (Day 16): bare propertyId for
 * PROPERTY, strip the ":<suffix>" for HOTEL and VEHICLE. FLIGHT always
 * resolves to empty (see InventoryDestinationConsumer's Javadoc) and
 * is logged and skipped, not guessed at.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AffinitySignalConsumer {

    private final RecordAffinitySignalUseCase    recordAffinityUseCase;
    private final RecordPopularitySignalUseCase  recordPopularityUseCase;
    private final DestinationLookupRepository    lookupRepository;
    private final ObjectMapper                    objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_SAVED_LOCATION_ADDED, groupId = "recommendation-service-group")
    public void onSavedLocationAdded(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node   = objectMapper.readTree(payload);
            String    userId = node.get("userId").asText();
            String    city    = node.path("city").asText(null);
            String    country = node.path("country").asText(null);

            if (city == null || city.isBlank() || country == null || country.isBlank()) {
                log.debug("SavedLocationAdded for user {} has no city/country — skipping", userId);
                ack.acknowledge();
                return;
            }

            recordAffinityUseCase.execute(userId, DestinationKey.of(city, country), AffinitySignalType.SAVED_LOCATION);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process SavedLocationAdded affinity signal: {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_COMPLETED, groupId = "recommendation-service-group")
    public void onBookingCompleted(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            String userId      = node.get("userId").asText();
            String bookingType = node.path("bookingType").asText("");
            String resourceId  = node.get("resourceId").asText();

            String lookupKey = extractLookupKey(bookingType, resourceId);
            if (lookupKey == null) {
                log.debug("Booking type {} has no resolvable destination — skipping (see ADR-012)", bookingType);
                ack.acknowledge();
                return;
            }

            Optional<DestinationKey> destination = lookupRepository.findByResourceKey(lookupKey);
            if (destination.isEmpty()) {
                log.debug("No destination lookup entry for key {} — skipping (inventory-created " +
                    "event may not have arrived yet)", lookupKey);
                ack.acknowledge();
                return;
            }

            recordAffinityUseCase.execute(userId, destination.get(), AffinitySignalType.COMPLETED_TRIP);
            recordPopularityUseCase.execute(destination.get());

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process BookingCompleted affinity/popularity signal: {}", ex.getMessage(), ex);
        }
    }

    /** Same per-type resourceId convention as review-service's BookingEventConsumer (Day 16). */
    private String extractLookupKey(String bookingType, String rawResourceId) {
        return switch (bookingType) {
            case "PROPERTY" -> rawResourceId;
            case "HOTEL"    -> rawResourceId.split(":")[0];
            case "VEHICLE"  -> rawResourceId.split(":")[0];
            default          -> null; // FLIGHT and any unrecognized type
        };
    }
}
