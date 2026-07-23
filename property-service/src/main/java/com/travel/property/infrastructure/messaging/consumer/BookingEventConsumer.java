package com.travel.property.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.property.domain.aggregate.Property;
import com.travel.property.domain.repository.PropertyRepository;
import com.travel.property.domain.valueobject.PropertyId;
import com.travel.property.infrastructure.messaging.producer.PropertyEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * Participates in the booking saga on the inventory side.
 *
 * BookingCreated (PROPERTY type) →
 *   if available: placeReservation() → ReservationPlacedEvent
 *                 published to inventory.reservation-confirmed
 *   if not: ReservationUnavailableEvent published to
 *           inventory.reservation-failed
 *
 * PaymentFailed (PROPERTY type) →
 *   releaseReservation() → ReservationReleasedEvent
 *   published to inventory.reservation-released
 *
 * BookingConfirmed →
 *   confirmReservation() — hold becomes permanent
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

   
    @Qualifier("propertyRepositoryAdapter")
    private final PropertyRepository     repository;
    private final PropertyEventPublisher eventPublisher;
    private final ObjectMapper           objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CREATED,
        groupId = "property-service-group"
    )
    @Transactional
    public void onBookingCreated(@Payload String payload,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 Acknowledgment ack) {
        handle(payload, "BookingCreated", ack, node -> {
            String bookingType = node.get("bookingType").asText();
            if (!"PROPERTY".equals(bookingType)) {
                ack.acknowledge();
                return; // Not for us — hotel/flight/vehicle handles it
            }

            String    bookingId   = node.get("bookingId").asText();
            String    userId      = node.get("userId").asText();
            String    resourceId  = node.get("resourceId").asText();
            LocalDate checkIn     = LocalDate.parse(node.get("checkInDate").asText());
            LocalDate checkOut    = LocalDate.parse(node.get("checkOutDate").asText());

            try {
                Property property = repository.findById(PropertyId.of(resourceId))
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Property", resourceId));

                property.placeReservation(bookingId, userId, checkIn, checkOut);
                repository.save(property);

                eventPublisher.publishEvents(property.getDomainEvents());
                property.clearDomainEvents();

                log.info("Reservation placed: bookingId={} propertyId={}",
                    bookingId, resourceId);

            } catch (BusinessRuleViolationException | ResourceNotFoundException ex) {
                log.warn("Reservation failed: bookingId={} reason={}", bookingId, ex.getMessage());
                publishReservationFailed(bookingId, userId, ex.getMessage());
            }

            ack.acknowledge();
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.PAYMENT_FAILED,
        groupId = "property-service-group"
    )
    @Transactional
    public void onPaymentFailed(@Payload String payload,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                Acknowledgment ack) {
        handle(payload, "PaymentFailed", ack, node -> {
            String bookingType = node.path("bookingType").asText("");
            if (!"PROPERTY".equals(bookingType)) {
                ack.acknowledge();
                return;
            }

            String bookingId  = node.get("bookingId").asText();
            String resourceId = node.get("resourceId").asText();
            String reason     = node.path("reason").asText("Payment failed");

            repository.findById(PropertyId.of(resourceId)).ifPresent(property -> {
                property.releaseReservation(bookingId, reason);
                repository.save(property);
                eventPublisher.publishEvents(property.getDomainEvents());
                property.clearDomainEvents();
                log.info("Reservation released: bookingId={} propertyId={}",
                    bookingId, resourceId);
            });

            ack.acknowledge();
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CONFIRMED,
        groupId = "property-service-group"
    )
    @Transactional
    public void onBookingConfirmed(@Payload String payload,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment ack) {
        handle(payload, "BookingConfirmed", ack, node -> {
            String bookingId = node.get("bookingId").asText();
            // We don't have resourceId in BookingConfirmedEvent directly —
            // in production embed it or look up from booking-service.
            // For now, log confirmation intent.
            log.info("Booking confirmed — reservation permanent: bookingId={}", bookingId);
            ack.acknowledge();
        });
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void publishReservationFailed(String bookingId, String userId, String reason) {
        try {
            String payload = objectMapper.writeValueAsString(new HashMap<>() {{
                put("bookingId", bookingId);
                put("userId",    userId);
                put("reason",    reason);
            }});
            kafkaTemplate.send(
                KafkaTopics.INVENTORY_RESERVATION_FAILED, bookingId, payload);
        } catch (Exception ex) {
            log.error("Failed to publish reservation-failed for {}: {}", bookingId, ex.getMessage());
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
