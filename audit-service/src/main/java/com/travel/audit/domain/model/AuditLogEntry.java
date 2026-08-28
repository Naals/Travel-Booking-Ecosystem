package com.travel.audit.domain.model;

import com.travel.audit.domain.valueobject.AuditLogId;
import com.travel.audit.domain.valueobject.ChainHash;
import com.travel.shared.domain.Entity;

import java.time.Instant;

/**
 * The platform's first genuinely immutable model.
 *
 * Extends Entity&lt;AuditLogId&gt;, not AggregateRoot — a deliberate,
 * different choice from both ends of the spectrum already established:
 * it has real, permanent, single-column identity like Booking or
 * Payment (unlike the composite-keyed plain projections search-service,
 * recommendation-service, and analytics-service used for their read
 * models — Days 14, 20, 22), but it has no mutation methods anywhere
 * in this class and never calls registerEvent(). AuditLogCreatedEvent
 * is constructed directly by RecordAuditEntryUseCase instead, since
 * there is no state-changing method here to raise it from — the first
 * time this platform needed Entity's identity semantics without
 * AggregateRoot's event-accumulation machinery, exactly as
 * shared-kernel's split (Day 2) was designed to allow.
 *
 * Two timestamps are kept deliberately distinct: occurredAt (the
 * source event's own occurredOn — when the fact actually happened)
 * and recordedAt (when audit-service itself observed and wrote it —
 * when the fact was ingested). The gap between them is a legitimate,
 * visible measure of consumer lag for a compliance system to expose,
 * not noise to collapse into one field.
 */
public final class AuditLogEntry extends Entity<AuditLogId> {

    private final long          sequenceNumber;
    private final AuditCategory category;
    private final String        sourceEventType;
    private final String        sourceEventId;
    private final String        subjectId;
    private final String        userId; // nullable
    private final String        summary;
    private final ChainHash     previousHash;
    private final ChainHash     contentHash;
    private final Instant       occurredAt;
    private final Instant       recordedAt;

    private AuditLogEntry(AuditLogId id, long sequenceNumber, AuditCategory category,
                          String sourceEventType, String sourceEventId, String subjectId,
                          String userId, String summary, ChainHash previousHash,
                          ChainHash contentHash, Instant occurredAt, Instant recordedAt) {
        super(id);
        this.sequenceNumber = sequenceNumber;
        this.category       = category;
        this.sourceEventType = sourceEventType;
        this.sourceEventId  = sourceEventId;
        this.subjectId      = subjectId;
        this.userId         = userId;
        this.summary        = summary;
        this.previousHash   = previousHash;
        this.contentHash    = contentHash;
        this.occurredAt     = occurredAt;
        this.recordedAt     = recordedAt;
    }

    /** The only construction path in normal operation — see RecordAuditEntryUseCase, the single call site. */
    public static AuditLogEntry create(AuditLogId id, long sequenceNumber, AuditCategory category,
                                       String sourceEventType, String sourceEventId, String subjectId,
                                       String userId, String summary, ChainHash previousHash,
                                       ChainHash contentHash, Instant occurredAt, Instant recordedAt) {
        return new AuditLogEntry(id, sequenceNumber, category, sourceEventType, sourceEventId,
            subjectId, userId, summary, previousHash, contentHash, occurredAt, recordedAt);
    }

    /**
     * Rehydrates from persistence. Named to match every other model's
     * convention since Day 7, even though — unlike any mutable
     * aggregate that convention was built for — there is no behavioral
     * difference between create() and reconstitute() here: nothing is
     * ever mutated after either one returns.
     */
    public static AuditLogEntry reconstitute(AuditLogId id, long sequenceNumber, AuditCategory category,
                                             String sourceEventType, String sourceEventId, String subjectId,
                                             String userId, String summary, ChainHash previousHash,
                                             ChainHash contentHash, Instant occurredAt, Instant recordedAt) {
        return new AuditLogEntry(id, sequenceNumber, category, sourceEventType, sourceEventId,
            subjectId, userId, summary, previousHash, contentHash, occurredAt, recordedAt);
    }

    // Getters only — no setters, no mutation methods anywhere in this class.

    public long          getSequenceNumber()  { return sequenceNumber; }
    public AuditCategory getCategory()        { return category; }
    public String        getSourceEventType() { return sourceEventType; }
    public String        getSourceEventId()   { return sourceEventId; }
    public String        getSubjectId()       { return subjectId; }
    public String        getUserId()          { return userId; }
    public String        getSummary()         { return summary; }
    public ChainHash     getPreviousHash()    { return previousHash; }
    public ChainHash     getContentHash()     { return contentHash; }
    public Instant       getOccurredAt()      { return occurredAt; }
    public Instant       getRecordedAt()      { return recordedAt; }
}
