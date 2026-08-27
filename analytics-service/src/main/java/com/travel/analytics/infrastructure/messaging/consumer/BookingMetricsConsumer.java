package com.travel.analytics.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.analytics.application.usecase.*;
import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.domain.repository.EventDeduplicationRepository;
import com.travel.common.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.handler.annotation.Payload;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * @Transactional is placed on each public @KafkaListener method, not
 * on the private handle() helper it calls — a deliberate detail, not
 * an oversight. Spring's proxy-based @Transactional relies on
 * external invocation to intercept the call; a call from handle() to
 * itself within the same class (self-invocation) would silently
 * bypass the proxy and run without a transaction. Putting the
 * annotation on the externally-invoked listener methods (called by
 * Spring Kafka's container, not by this class) is what makes the
 * dedup-check-then-mutate sequence inside handle() atomic. Same
 * placement convention property-service's BookingEventConsumer
 * (Day 10) already used for its own read-then-write sequence — not a
 * new pattern, just correctly reused where the same reasoning applies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingMetricsConsumer {

    private final RecordBookingCreatedUseCase   createdUseCase;
    private final RecordBookingConfirmedUseCase confirmedUseCase;
    private final RecordBookingCancelledUseCase cancelledUseCase;
    private final RecordBookingCompletedUseCase completedUseCase;
    private final EventDeduplicationRepository  deduplicationRepository;
    private final ObjectMapper                   objectMapper;

    @KafkaListener(topics = KafkaTopics.BOOKING_CREATED, groupId = "analytics-service-group")
    @Transactional
    public void onBookingCreated(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingCreated", ack, node -> {
            String      bookingId = node.get("bookingId").asText();
            BookingType type      = BookingType.valueOf(node.get("bookingType").asText());
            createdUseCase.execute(bookingId, type, dateOf(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_CONFIRMED, groupId = "analytics-service-group")
    @Transactional
    public void onBookingConfirmed(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingConfirmed", ack, node ->
            confirmedUseCase.execute(node.get("bookingId").asText(), dateOf(node)));
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_CANCELLED, groupId = "analytics-service-group")
    @Transactional
    public void onBookingCancelled(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingCancelled", ack, node ->
            cancelledUseCase.execute(node.get("bookingId").asText(), dateOf(node)));
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_COMPLETED, groupId = "analytics-service-group")
    @Transactional
    public void onBookingCompleted(@Payload String payload, Acknowledgment ack) {
        handle(payload, "BookingCompleted", ack, node -> {
            BookingType type = BookingType.valueOf(node.get("bookingType").asText());
            completedUseCase.execute(type, dateOf(node));
        });
    }

    /**
     * occurredOn (DomainEvent, shared-kernel Day 2) drives date-
     * bucketing rather than local processing-time Instant.now() —
     * avoids skewing a metric into the wrong day if Kafka consumer lag
     * pushes real-time processing past midnight. Parses correctly as
     * ISO-8601 thanks to config-server's shared Jackson setting
     * (write-dates-as-timestamps: false, Day 4).
     */
    private LocalDate dateOf(JsonNode node) {
        return Instant.parse(node.get("occurredOn").asText()).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private void handle(String payload, String eventType, Acknowledgment ack, ConsumerStep step) {
        try {
            JsonNode node    = objectMapper.readTree(payload);
            String    eventId = node.get("eventId").asText();

            if (!deduplicationRepository.markProcessedIfNew(eventId)) {
                log.debug("Duplicate {} (eventId={}) — already processed, skipping", eventType, eventId);
                ack.acknowledge();
                return;
            }

            step.execute(node);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process {}: {}", eventType, ex.getMessage(), ex);
            // Not acknowledged — redelivery is safe, markProcessedIfNew
            // rolls back with the rest of this transaction on failure.
        }
    }

    @FunctionalInterface
    interface ConsumerStep {
        void execute(JsonNode node) throws Exception;
    }
}
