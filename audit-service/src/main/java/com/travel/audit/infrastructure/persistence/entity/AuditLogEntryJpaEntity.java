package com.travel.audit.infrastructure.persistence.entity;

import com.travel.audit.domain.model.AuditCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * No @PrePersist/@PreUpdate here, unlike every other JPA entity in
 * this platform since Day 6 — occurredAt and recordedAt are always
 * explicitly supplied by RecordAuditEntryUseCase before insert, since
 * they are meaningful domain facts (when did this really happen, when
 * did we really observe it), not infrastructure bookkeeping fields to
 * let JPA fill in automatically.
 */
@Entity
@Table(name = "audit_log_entries", indexes = {
    @Index(name = "idx_audit_log_subject_id", columnList = "subject_id"),
    @Index(name = "idx_audit_log_user_id",    columnList = "user_id"),
    @Index(name = "idx_audit_log_sequence",   columnList = "sequence_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogEntryJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "sequence_number", nullable = false, unique = true)
    private long sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AuditCategory category;

    @Column(name = "source_event_type", nullable = false)
    private String sourceEventType;

    @Column(name = "source_event_id", nullable = false, unique = true)
    private String sourceEventId;

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "summary", nullable = false, length = 500)
    private String summary;

    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}
