package com.travel.audit.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Constructed directly by RecordAuditEntryUseCase after a successful,
 * non-duplicate write — not accumulated via AggregateRoot.registerEvent()
 * the way every other event in this platform has been since Day 6,
 * because AuditLogEntry deliberately isn't an AggregateRoot (see its
 * class Javadoc). Still a valid shared-kernel DomainEvent subclass;
 * getAggregateId() follows the same convention every other event
 * uses — the ID of the thing the event is about.
 *
 * The final, sixth instance of a KafkaTopics constant seeded on Day 3
 * finally getting a real producer — after REVIEW_CREATED (Day 3→16),
 * MESSAGE_SENT (Day 3→17), WALLET_CREDITED (Day 3→18),
 * LOYALTY_POINTS_EARNED (Day 3→19), and FRAUD_ALERT_RAISED (Day 3→21).
 * AUDIT_LOG_CREATED was literally the last topic declared in that
 * original file, immediately before DLQ_SUFFIX — the last thing
 * written on Day 3 is the last thing implemented, on Day 23.
 */
public class AuditLogCreatedEvent extends DomainEvent {

    private final String auditLogId;
    private final long   sequenceNumber;
    private final String category;
    private final String sourceEventType;
    private final String subjectId;

    public AuditLogCreatedEvent(String auditLogId, long sequenceNumber, String category,
                                String sourceEventType, String subjectId) {
        super("AuditLogCreated");
        this.auditLogId     = auditLogId;
        this.sequenceNumber = sequenceNumber;
        this.category       = category;
        this.sourceEventType = sourceEventType;
        this.subjectId      = subjectId;
    }

    @Override public String getAggregateId()   { return auditLogId; }
    public String getAuditLogId()               { return auditLogId; }
    public long   getSequenceNumber()           { return sequenceNumber; }
    public String getCategory()                 { return category; }
    public String getSourceEventType()          { return sourceEventType; }
    public String getSubjectId()                { return subjectId; }
}
