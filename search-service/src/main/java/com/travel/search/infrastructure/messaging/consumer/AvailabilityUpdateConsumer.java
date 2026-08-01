package com.travel.search.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.search.application.usecase.UpdateAvailabilityUseCase;
import com.travel.search.domain.model.ListingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumes availability-change signals from the four inventory services.
 *
 * Three of these (property, hotel, flight-seat, vehicle) only carry the
 * listing ID — the same event class fires whether a hold was placed or
 * released, so this service cannot infer the new state from the event
 * alone. touchSignal() records that *something* changed without
 * asserting what; see ADR-007 for the production reconciliation path.
 *
 * flight.status-changed is the one exception — it carries an explicit
 * newStatus field, so a transition to CANCELLED is handled with a firm
 * markUnavailable() call.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvailabilityUpdateConsumer {

    private final UpdateAvailabilityUseCase useCase;
    private final ObjectMapper              objectMapper;

    @KafkaListener(topics = KafkaTopics.PROPERTY_AVAILABILITY_UPDATED, groupId = "search-service-group")
    public void onPropertyAvailabilityUpdated(@Payload String payload, Acknowledgment ack) {
        handle(payload, ack, node ->
            useCase.touchSignal(node.get("propertyId").asText(), ListingType.PROPERTY));
    }

    @KafkaListener(topics = KafkaTopics.HOTEL_ROOM_AVAILABILITY_UPDATED, groupId = "search-service-group")
    public void onHotelRoomAvailabilityUpdated(@Payload String payload, Acknowledgment ack) {
        handle(payload, ack, node ->
            useCase.touchSignal(node.get("hotelId").asText(), ListingType.HOTEL));
    }

    @KafkaListener(topics = KafkaTopics.FLIGHT_SEAT_AVAILABILITY_UPDATED, groupId = "search-service-group")
    public void onFlightSeatAvailabilityUpdated(@Payload String payload, Acknowledgment ack) {
        handle(payload, ack, node ->
            useCase.touchSignal(node.get("flightId").asText(), ListingType.FLIGHT));
    }

    @KafkaListener(topics = KafkaTopics.VEHICLE_AVAILABILITY_UPDATED, groupId = "search-service-group")
    public void onVehicleAvailabilityUpdated(@Payload String payload, Acknowledgment ack) {
        handle(payload, ack, node ->
            useCase.touchSignal(node.get("vehicleId").asText(), ListingType.VEHICLE));
    }

    /**
     * Handles the one availability-related event that carries firm state.
     */
    @KafkaListener(topics = "flight.status-changed", groupId = "search-service-group")
    public void onFlightStatusChanged(@Payload String payload, Acknowledgment ack) {
        handle(payload, ack, node -> {
            String flightId  = node.get("flightId").asText();
            String newStatus = node.get("newStatus").asText();
            if ("CANCELLED".equals(newStatus)) {
                useCase.markUnavailable(flightId, ListingType.FLIGHT);
            } else {
                useCase.touchSignal(flightId, ListingType.FLIGHT);
            }
        });
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private void handle(String payload, Acknowledgment ack, ConsumerStep step) {
        try {
            step.execute(objectMapper.readTree(payload));
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process availability signal: {}", ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    interface ConsumerStep {
        void execute(JsonNode node) throws Exception;
    }
}
