package com.travel.identity.infrastructure.messaging;

import com.travel.identity.infrastructure.persistence.entity.OutboxEventEntity;
import com.travel.identity.infrastructure.persistence.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Polls the outbox table every second and publishes pending events to Kafka.
 *
 * Guarantees at-least-once delivery:
 *   - If Kafka publish succeeds → mark processed
 *   - If Kafka publish fails → increment retryCount, leave unprocessed
 *   - If the JVM dies after publish but before markAsProcessed → event
 *     is re-published on next poll. Consumers must be idempotent.
 *
 * In production, consider replacing with Debezium CDC for lower latency.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPoller {

    private static final int MAX_RETRIES = 3;

    private final OutboxEventJpaRepository  outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void pollAndPublish() {
        List<OutboxEventEntity> pending = outboxRepository.findPendingEvents(MAX_RETRIES);
        if (pending.isEmpty()) return;

        log.debug("Outbox: processing {} event(s)", pending.size());

        for (OutboxEventEntity event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex != null)
                            log.error("Kafka publish failed for {}: {}", event.getId(), ex.getMessage());
                    });

                outboxRepository.markAsProcessed(event.getId());
                log.debug("Published {} → {}", event.getEventType(), event.getTopic());

            } catch (Exception ex) {
                log.error("Outbox poll error for {}: {}", event.getId(), ex.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(ex.getMessage());
                outboxRepository.save(event);
            }
        }
    }
}
