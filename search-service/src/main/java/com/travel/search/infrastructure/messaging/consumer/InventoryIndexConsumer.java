package com.travel.search.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.search.application.usecase.IndexListingUseCase;
import com.travel.search.domain.model.ListingType;
import com.travel.search.domain.model.SearchDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes "listing created" events from the four Tier 2 inventory
 * services and indexes them into the unified listings index.
 *
 * Each handler maps only the fields the source event actually carries
 * (see each service's Day 10-13 event classes). price and geo
 * coordinates are left null where the upstream event doesn't include
 * them — see ADR-007 for the reconciliation strategy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryIndexConsumer {

    private final IndexListingUseCase indexUseCase;
    private final ObjectMapper        objectMapper;

    @KafkaListener(topics = KafkaTopics.SEARCH_INDEX_PROPERTY, groupId = "search-service-group")
    public void onPropertyCreated(@Payload String payload, Acknowledgment ack) {
        handle(payload, "PropertyCreated", ack, node -> {
            SearchDocument doc = SearchDocument.builder()
                .id(node.get("propertyId").asText())
                .listingType(ListingType.PROPERTY)
                .title(node.get("title").asText())
                .city(node.get("city").asText())
                .country(node.get("country").asText())
                .available(true)
                .attributes(Map.of(
                    "propertyType", node.get("propertyType").asText(),
                    "hostId",       node.get("hostId").asText()))
                .build();
            indexUseCase.execute(doc);
        });
    }

    @KafkaListener(topics = KafkaTopics.SEARCH_INDEX_HOTEL, groupId = "search-service-group")
    public void onHotelCreated(@Payload String payload, Acknowledgment ack) {
        handle(payload, "HotelCreated", ack, node -> {
            SearchDocument doc = SearchDocument.builder()
                .id(node.get("hotelId").asText())
                .listingType(ListingType.HOTEL)
                .title(node.get("name").asText())
                .city(node.get("city").asText())
                .country(node.get("country").asText())
                .rating(node.get("starRating").asDouble())
                .available(true)
                .attributes(Map.of(
                    "starRating", node.get("starRating").asText()))
                .build();
            indexUseCase.execute(doc);
        });
    }

    @KafkaListener(topics = KafkaTopics.SEARCH_INDEX_FLIGHT, groupId = "search-service-group")
    public void onFlightScheduled(@Payload String payload, Acknowledgment ack) {
        handle(payload, "FlightScheduled", ack, node -> {
            String origin      = node.get("originCode").asText();
            String destination = node.get("destinationCode").asText();
            String flightNo     = node.get("flightNumber").asText();

            SearchDocument doc = SearchDocument.builder()
                .id(node.get("flightId").asText())
                .listingType(ListingType.FLIGHT)
                .title(flightNo + ": " + origin + " → " + destination)
                .available(true)
                .attributes(Map.of(
                    "flightNumber",    flightNo,
                    "originCode",      origin,
                    "destinationCode", destination,
                    "departureTime",   node.get("departureTime").asText()))
                .build();
            indexUseCase.execute(doc);
        });
    }

    @KafkaListener(topics = KafkaTopics.SEARCH_INDEX_VEHICLE, groupId = "search-service-group")
    public void onVehicleAddedToFleet(@Payload String payload, Acknowledgment ack) {
        handle(payload, "VehicleAddedToFleet", ack, node -> {
            String category = node.get("category").asText();
            String city      = node.get("city").asText();

            SearchDocument doc = SearchDocument.builder()
                .id(node.get("vehicleId").asText())
                .listingType(ListingType.VEHICLE)
                .title(category + " rental in " + city)
                .city(city)
                .country(node.get("country").asText())
                .available(true)
                .attributes(Map.of(
                    "category",     category,
                    "locationCode", node.get("locationCode").asText()))
                .build();
            indexUseCase.execute(doc);
        });
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private void handle(String payload, String eventType,
                        Acknowledgment ack, ConsumerStep step) {
        try {
            step.execute(objectMapper.readTree(payload));
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to index from {}: {}", eventType, ex.getMessage(), ex);
            // Not acknowledged — redelivered; indexing is idempotent by id
        }
    }

    @FunctionalInterface
    interface ConsumerStep {
        void execute(JsonNode node) throws Exception;
    }
}
