package com.travel.identity.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.identity.infrastructure.persistence.entity.OutboxEventEntity;
import com.travel.identity.infrastructure.persistence.repository.OutboxEventJpaRepository;
import com.travel.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

/**
 * Writes domain events to the outbox table inside the current transaction.
 * Actual Kafka publishing is handled by OutboxEventPoller asynchronously.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper             objectMapper;

    public void publishEvents(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            try {
                String topic = resolveTopic(event);
                if (topic == null) { log.warn("No topic for: {}", event.getEventType()); continue; }

                outboxRepository.save(OutboxEventEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateId(event.getAggregateId())
                    .aggregateType("User")
                    .eventType(event.getEventType())
                    .topic(topic)
                    .payload(objectMapper.writeValueAsString(event))
                    .build());

            } catch (Exception ex) {
                log.error("Outbox write failed for {}: {}", event.getEventType(), ex.getMessage());
                throw new RuntimeException("Outbox write failed", ex);
            }
        }
    }

    private String resolveTopic(DomainEvent event) {
        return switch (event.getEventType()) {
            case "UserRegistered"  -> KafkaTopics.USER_REGISTERED;
            case "UserDeactivated" -> KafkaTopics.USER_DEACTIVATED;
            default -> null;
        };
    }
}
