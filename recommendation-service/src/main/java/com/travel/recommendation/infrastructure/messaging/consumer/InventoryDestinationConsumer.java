package com.travel.recommendation.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.recommendation.application.usecase.RecordDestinationLookupUseCase;
import com.travel.recommendation.domain.valueobject.DestinationKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Populates DestinationLookup from the three inventory-created events
 * that carry city+country directly. No FLIGHT listener exists —
 * FlightScheduledEvent (Day 12) carries only IATA airport codes, never
 * a resolvable city/country. See ADR-012.
 *
 * Subscribes to the same search.index-* topics search-service already
 * consumes (Day 14) — named for their original consumer, now reused
 * here for a genuinely different purpose (destination correlation, not
 * search indexing). Left as-is rather than renamed; see ADR-012.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryDestinationConsumer {

    private final RecordDestinationLookupUseCase useCase;
    private final ObjectMapper                    objectMapper;

    @KafkaListener(topics = KafkaTopics.SEARCH_INDEX_PROPERTY, groupId = "recommendation-service-group")
    public void onPropertyCreated(@Payload String payload, Acknowledgment ack) {
        handle(payload, "PropertyCreated", ack, node ->
            useCase.execute(node.get("propertyId").asText(),
                DestinationKey.of(node.get("city").asText(), node.get("country").asText())));
    }

    @KafkaListener(topics = KafkaTopics.SEARCH_INDEX_HOTEL, groupId = "recommendation-service-group")
    public void onHotelCreated(@Payload String payload, Acknowledgment ack) {
        handle(payload, "HotelCreated", ack, node ->
            useCase.execute(node.get("hotelId").asText(),
                DestinationKey.of(node.get("city").asText(), node.get("country").asText())));
    }

    @KafkaListener(topics = KafkaTopics.SEARCH_INDEX_VEHICLE, groupId = "recommendation-service-group")
    public void onVehicleAddedToFleet(@Payload String payload, Acknowledgment ack) {
        handle(payload, "VehicleAddedToFleet", ack, node ->
            useCase.execute(node.get("locationCode").asText(),
                DestinationKey.of(node.get("city").asText(), node.get("country").asText())));
    }

    private void handle(String payload, String eventType, Acknowledgment ack, ConsumerStep step) {
        try {
            step.execute(objectMapper.readTree(payload));
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process {} for destination lookup: {}", eventType, ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    interface ConsumerStep {
        void execute(JsonNode node) throws Exception;
    }
}
