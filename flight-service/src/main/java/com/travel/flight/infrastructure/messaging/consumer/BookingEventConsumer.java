package com.travel.flight.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;
import com.travel.flight.domain.aggregate.Flight;
import com.travel.flight.domain.repository.FlightRepository;
import com.travel.flight.domain.valueobject.FlightId;
import com.travel.flight.domain.valueobject.SeatClass;
import com.travel.flight.infrastructure.messaging.producer.FlightEventPublisher;
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

import java.util.HashMap;

/**
 * Participates in the booking saga for FLIGHT type bookings.
 *
 * resourceId format for flight bookings: "<flightId>:<seatClass>"
 * e.g. "uuid-flight-123:ECONOMY" or "uuid-flight-123:BUSINESS"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final FlightRepository     repository;
    private final FlightEventPublisher eventPublisher;
    private final ObjectMapper         objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CREATED,
        groupId = "flight-service-group"
    )
    @Transactional
    public void onBookingCreated(@Payload String payload,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 Acknowledgment ack) {
        handle(payload, "BookingCreated", ack, node -> {
            String bookingType = node.get("bookingType").asText();
            if (!"FLIGHT".equals(bookingType)) { ack.acknowledge(); return; }

            String bookingId  = node.get("bookingId").asText();
            String userId     = node.get("userId").asText();
            String resourceId = node.get("resourceId").asText();

            // resourceId = "<flightId>:<seatClass>"
            String[] parts     = resourceId.split(":");
            String   flightId  = parts[0];
            SeatClass seatClass = parts.length > 1
                ? SeatClass.valueOf(parts[1].toUpperCase())
                : SeatClass.ECONOMY;

            try {
                Flight flight = repository.findById(FlightId.of(flightId))
                    .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));

                flight.reserveSeat(bookingId, userId, seatClass);
                repository.save(flight);
                eventPublisher.publishEvents(flight.getDomainEvents());
                flight.clearDomainEvents();

                log.info("Seat reserved: bookingId={} flight={} class={}",
                    bookingId, flightId, seatClass);

            } catch (BusinessRuleViolationException | ResourceNotFoundException ex) {
                log.warn("Seat reservation failed: bookingId={} reason={}",
                    bookingId, ex.getMessage());
                publishReservationFailed(bookingId, userId, ex.getMessage());
            }

            ack.acknowledge();
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.PAYMENT_FAILED,
        groupId = "flight-service-group"
    )
    @Transactional
    public void onPaymentFailed(@Payload String payload,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                Acknowledgment ack) {
        handle(payload, "PaymentFailed", ack, node -> {
            String bookingType = node.path("bookingType").asText("");
            if (!"FLIGHT".equals(bookingType)) { ack.acknowledge(); return; }

            String bookingId  = node.get("bookingId").asText();
            String resourceId = node.get("resourceId").asText();
            String reason     = node.path("reason").asText("Payment failed");
            String flightId   = resourceId.split(":")[0];

            repository.findById(FlightId.of(flightId)).ifPresent(flight -> {
                try {
                    flight.releaseReservation(bookingId, reason);
                    repository.save(flight);
                    eventPublisher.publishEvents(flight.getDomainEvents());
                    flight.clearDomainEvents();
                    log.info("Seat reservation released: bookingId={} flight={}",
                        bookingId, flightId);
                } catch (BusinessRuleViolationException ex) {
                    log.warn("Release failed (may be idempotent): {}", ex.getMessage());
                }
            });

            ack.acknowledge();
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CONFIRMED,
        groupId = "flight-service-group"
    )
    @Transactional
    public void onBookingConfirmed(@Payload String payload,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment ack) {
        handle(payload, "BookingConfirmed", ack, node -> {
            log.info("Booking confirmed — seat permanently occupied: {}",
                node.get("bookingId").asText());
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
