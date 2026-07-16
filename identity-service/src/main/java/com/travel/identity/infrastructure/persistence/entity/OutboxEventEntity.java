package com.travel.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Outbox table — guarantees at-least-once Kafka delivery.
 *
 * Events are written here atomically with the aggregate state change
 * inside a single DB transaction. The OutboxEventPoller reads unprocessed
 * rows and publishes them to Kafka asynchronously. If publishing fails,
 * retryCount is incremented and the row stays unprocessed until
 * MAX_RETRIES is reached, at which point the row is left for manual
 * inspection (alerting should fire on high retryCount rows).
 */
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_processed",  columnList = "processed"),
    @Index(name = "idx_outbox_created_at", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEventEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "aggregate_id",   nullable = false) private String aggregateId;
    @Column(name = "aggregate_type", nullable = false) private String aggregateType;
    @Column(name = "event_type",     nullable = false) private String eventType;
    @Column(name = "topic",          nullable = false) private String topic;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "processed",   nullable = false) private boolean processed;
    @Column(name = "processed_at")                  private Instant processedAt;
    @Column(name = "retry_count", nullable = false) private int     retryCount;
    @Column(name = "last_error",  columnDefinition = "TEXT") private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt  = Instant.now();
        processed  = false;
        retryCount = 0;
    }
}
