package com.travel.hotel.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;
import com.travel.hotel.domain.aggregate.Hotel;
import com.travel.hotel.domain.repository.HotelRepository;
import com.travel.hotel.domain.valueobject.HotelId;
import com.travel.hotel.domain.valueobject.RoomType;
import com.travel.hotel.infrastructure.messaging.producer.HotelEventPublisher;
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
 * Participates in the booking saga for HOTEL type bookings.
 * Mirrors the same choreography pattern as property-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    @Qualifier("hotelRepositoryAdapter")
    private final HotelRepository     repository;
    private final HotelEventPublisher eventPublisher;
    private final ObjectMapper        objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CREATED,
        groupId = "hotel-service-group"
    )
    @Transactional
    public void onBookingCreated(@Payload String payload,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 Acknowledgment ack) {
        handle(payload, "BookingCreated", ack, node -> {
            String bookingType = node.get("bookingType").asText();
            if (!"HOTEL".equals(bookingType)) { ack.acknowledge(); return; }

            String    bookingId   = node.get("bookingId").asText();
            String    userId      = node.get("userId").asText();
            String    resourceId  = node.get("resourceId").asText();
            LocalDate checkIn     = LocalDate.parse(node.get("checkInDate").asText());
            LocalDate checkOut    = LocalDate.parse(node.get("checkOutDate").asText());

            // resourceId format for hotel bookings: "<hotelId>:<roomType>"
            String[] parts   = resourceId.split(":");
            String   hotelId = parts[0];
            RoomType roomType = parts.length > 1
                ? RoomType.valueOf(parts[1].toUpperCase())
                : RoomType.STANDARD;

            try {
                Hotel hotel = repository.findById(HotelId.of(hotelId))
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

                hotel.reserveRoom(bookingId, userId, roomType, checkIn, checkOut);
                repository.save(hotel);
                eventPublisher.publishEvents(hotel.getDomainEvents());
                hotel.clearDomainEvents();

                log.info("Room reserved: bookingId={} hotel={}", bookingId, hotelId);

            } catch (BusinessRuleViolationException | ResourceNotFoundException ex) {
                log.warn("Room reservation failed: bookingId={} reason={}",
                    bookingId, ex.getMessage());
                publishReservationFailed(bookingId, userId, ex.getMessage());
            }

            ack.acknowledge();
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.PAYMENT_FAILED,
        groupId = "hotel-service-group"
    )
    @Transactional
    public void onPaymentFailed(@Payload String payload,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                Acknowledgment ack) {
        handle(payload, "PaymentFailed", ack, node -> {
            String bookingType = node.path("bookingType").asText("");
            if (!"HOTEL".equals(bookingType)) { ack.acknowledge(); return; }

            String bookingId  = node.get("bookingId").asText();
            String resourceId = node.get("resourceId").asText();
            String reason     = node.path("reason").asText("Payment failed");
            String hotelId    = resourceId.split(":")[0];

            repository.findById(HotelId.of(hotelId)).ifPresent(hotel -> {
                hotel.releaseReservation(bookingId, reason);
                repository.save(hotel);
                eventPublisher.publishEvents(hotel.getDomainEvents());
                hotel.clearDomainEvents();
                log.info("Room reservation released: bookingId={} hotel={}", bookingId, hotelId);
            });

            ack.acknowledge();
        });
    }

    @KafkaListener(
        topics  = KafkaTopics.BOOKING_CONFIRMED,
        groupId = "hotel-service-group"
    )
    @Transactional
    public void onBookingConfirmed(@Payload String payload,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment ack) {
        handle(payload, "BookingConfirmed", ack, node -> {
            log.info("Booking confirmed — room reservation permanent: {}",
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
