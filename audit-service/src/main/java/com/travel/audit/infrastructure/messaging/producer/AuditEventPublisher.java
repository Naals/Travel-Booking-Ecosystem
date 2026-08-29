package com.travel.audit.infrastructure.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.audit.domain.event.AuditLogCreatedEvent;
import com.travel.common.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Simpler than every prior publisher in this platform — no switch
 * statement, since AuditLogCreatedEvent is the only event type this
 * service will ever produce (there is exactly one topic to route to,
 * KafkaTopics.AUDIT_LOG_CREATED). Called directly by
 * RecordAuditEntryUseCase, not fed a List the way every other
 * publisher drains from an aggregate's accumulated events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper                  objectMapper;

    public void publish(AuditLogCreatedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.AUDIT_LOG_CREATED, event.getAggregateId(),
                objectMapper.writeValueAsString(event));
            log.debug("Published AuditLogCreated → {}", KafkaTopics.AUDIT_LOG_CREATED);
        } catch (Exception ex) {
            log.error("Failed to publish AuditLogCreated: {}", ex.getMessage(), ex);
            throw new RuntimeException("Event publish failed", ex);
        }
    }
}
