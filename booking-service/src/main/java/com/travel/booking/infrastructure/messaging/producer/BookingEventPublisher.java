package com.travel.booking.infrastructure.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.booking.domain.event.*;
import com.travel.common.event.KafkaTopics;
import com.travel.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Publishes booking domain events to Kafka.
 * Partition key = aggregateId (bookingId) — guarantees per-booking ordering.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper                  objectMapper;

    public void publishEvents(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            try {
                String topic = resolveTopic(event);
                if (topic == null) { log.warn("No topic for: {}", event.getEventType()); continue; }
                kafkaTemplate.send(topic, event.getAggregateId(),
                    objectMapper.writeValueAsString(event));
                log.debug("Published {} → {}", event.getEventType(), topic);
            } catch (Exception ex) {
                log.error("Failed to publish {}: {}", event.getEventType(), ex.getMessage(), ex);
                throw new RuntimeException("Event publish failed", ex);
            }
        }
    }

    private String resolveTopic(DomainEvent event) {
        return switch (event.getEventType()) {
            case "BookingCreated"    -> KafkaTopics.BOOKING_CREATED;
            case "InventoryReserved" -> KafkaTopics.INVENTORY_RESERVATION_CONFIRMED;
            case "BookingConfirmed"  -> KafkaTopics.BOOKING_CONFIRMED;
            case "BookingCancelled"  -> KafkaTopics.BOOKING_CANCELLED;
            case "BookingCompleted"  -> KafkaTopics.BOOKING_COMPLETED;
            case "PaymentFailed"     -> KafkaTopics.PAYMENT_FAILED;
            default                  -> null;
        };
    }
}
