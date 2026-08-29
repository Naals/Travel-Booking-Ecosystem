package com.travel.audit.application.dto.response;

import com.travel.audit.domain.model.AuditLogEntry;
import java.time.Instant;

public record AuditLogEntryResponse(
    String  auditLogId,
    long    sequenceNumber,
    String  category,
    String  sourceEventType,
    String  subjectId,
    String  userId,
    String  summary,
    String  previousHash,
    String  contentHash,
    Instant occurredAt,
    Instant recordedAt
) {
    public static AuditLogEntryResponse from(AuditLogEntry e) {
        return new AuditLogEntryResponse(
            e.getId().getValue(), e.getSequenceNumber(), e.getCategory().name(),
            e.getSourceEventType(), e.getSubjectId(), e.getUserId(), e.getSummary(),
            e.getPreviousHash().getValue(), e.getContentHash().getValue(),
            e.getOccurredAt(), e.getRecordedAt());
    }
}
