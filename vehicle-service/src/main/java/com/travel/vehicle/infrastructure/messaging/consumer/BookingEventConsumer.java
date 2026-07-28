package com.travel.vehicle.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.vehicle.domain.aggregate.Vehicle;
import com.travel.vehicle.domain.repository.VehicleRepository;
import com.travel.vehicle.domain.service.FleetQueryService;
import com.travel.vehicle.domain.valueobject.PickupLocation;
import com.travel.vehicle.domain.valueobject.VehicleCategory;
import com.travel.vehicle.infrastructure.messaging.producer.VehicleEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;

/**
 * Participates in the booking saga for VEHICLE type bookings.
 *
 * resourceId format for vehicle bookings:
 *   "<locationCode>:<category>"
 *   e.g. "IST:ECONOMY" or "JFK:SUV"
 *
 * For one-way rentals, the booking metadata carries a return location.
 * This is a common pattern in car rental platforms — the customer
 * picks up in Istanbul and returns in Ankara.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final FleetQueryService    fleetQueryService;
    private final VehicleRepository    repository;
    private final VehicleEventPublisher eventPublisher;
    private final ObjectMapper          objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CREATED,
        groupId = "vehicle-service-group"
    )
    @Transactional
    public void onBookingCreated(@Payload String payload,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 Acknowledgment ack) {
        handle(payload, "BookingCreated", ack, node -> {
            String bookingType = node.get("bookingType").asText();
            if (!"VEHICLE".equals(bookingType)) { ack.acknowledge(); return; }

            String bookingId  = node.get("bookingId").asText();
            String userId     = node.get("userId").asText();
            String resourceId = node.get("resourceId").asText();

            // resourceId = "<locationCode>:<category>"
            String[] parts        = resourceId.split(":");
            String   locationCode = parts[0];
            VehicleCategory category = parts.length > 1
                ? VehicleCategory.valueOf(parts[1].toUpperCase())
                : VehicleCategory.ECONOMY;

            LocalDate pickupDate = LocalDate.parse(node.get("checkInDate").asText());
            LocalDate returnDate = LocalDate.parse(node.get("checkOutDate").asText());

            try {
                Vehicle vehicle = fleetQueryService.findFirstAvailable(
                    category, locationCode, pickupDate, returnDate);

                PickupLocation pickupLoc = vehicle.getCurrentLocation();

                // Return location may differ for one-way (omitted in MVP — same location)
                vehicle.reserve(bookingId, userId, pickupDate, returnDate,
                    pickupLoc, pickupLoc);

                repository.save(vehicle);
                eventPublisher.publishEvents(vehicle.getDomainEvents());
                vehicle.clearDomainEvents();

                log.info("Vehicle reserved: bookingId={} vehicleId={} category={}",
                    bookingId, vehicle.getId().getValue(), category);

            } catch (BusinessRuleViolationException ex) {
                log.warn("Vehicle reservation failed: bookingId={} reason={}",
                    bookingId, ex.getMessage());
                publishReservationFailed(bookingId, userId, ex.getMessage());
            }

            ack.acknowledge();
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.PAYMENT_FAILED,
        groupId = "vehicle-service-group"
    )
    @Transactional
    public void onPaymentFailed(@Payload String payload,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                Acknowledgment ack) {
        handle(payload, "PaymentFailed", ack, node -> {
            String bookingType = node.path("bookingType").asText("");
            if (!"VEHICLE".equals(bookingType)) { ack.acknowledge(); return; }

            String bookingId = node.get("bookingId").asText();
            String reason    = node.path("reason").asText("Payment failed");

            // Find vehicle by bookingId across the fleet
            repository.findByStatus(
                    com.travel.vehicle.domain.valueobject.VehicleStatus.RESERVED)
                .stream()
                .filter(v -> v.getActiveRental()
                    .map(r -> r.getBookingId().equals(bookingId))
                    .orElse(false))
                .findFirst()
                .ifPresent(vehicle -> {
                    vehicle.releaseReservation(bookingId, reason);
                    repository.save(vehicle);
                    eventPublisher.publishEvents(vehicle.getDomainEvents());
                    vehicle.clearDomainEvents();
                    log.info("Vehicle reservation released: bookingId={}", bookingId);
                });

            ack.acknowledge();
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CONFIRMED,
        groupId = "vehicle-service-group"
    )
    @Transactional
    public void onBookingConfirmed(@Payload String payload,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment ack) {
        handle(payload, "BookingConfirmed", ack, node -> {
            String bookingId = node.get("bookingId").asText();

            repository.findByStatus(
                    com.travel.vehicle.domain.valueobject.VehicleStatus.RESERVED)
                .stream()
                .filter(v -> v.getActiveRental()
                    .map(r -> r.getBookingId().equals(bookingId))
                    .orElse(false))
                .findFirst()
                .ifPresent(vehicle -> {
                    vehicle.confirmRental(bookingId);
                    repository.save(vehicle);
                    log.info("Vehicle rental confirmed: bookingId={} vehicleId={}",
                        bookingId, vehicle.getId().getValue());
                });

            ack.acknowledge();
        });
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void publishReservationFailed(String bookingId, String userId, String reason) {
        try {
            var payload = new HashMap<String, String>() {{
                put("bookingId", bookingId);
                put("userId",    userId);
                put("reason",    reason);
            }};
            kafkaTemplate.send(KafkaTopics.INVENTORY_RESERVATION_FAILED, bookingId,
                objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            log.error("Failed to publish reservation-failed: {}", ex.getMessage());
        }
    }

    private void handle(String payload, String eventType,
                        Acknowledgment ack, ConsumerStep step) {
        try {
            step.execute(objectMapper.readTree(payload));
        } catch (Exception ex) {
            log.error("Consumer error for {}: {}", eventType, ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    interface ConsumerStep {
        void execute(JsonNode node) throws Exception;
    }
}
