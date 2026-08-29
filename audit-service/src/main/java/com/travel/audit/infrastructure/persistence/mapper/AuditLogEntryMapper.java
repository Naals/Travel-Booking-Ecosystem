package com.travel.audit.infrastructure.persistence.mapper;

import com.travel.audit.domain.model.AuditLogEntry;
import com.travel.audit.domain.valueobject.AuditLogId;
import com.travel.audit.domain.valueobject.ChainHash;
import com.travel.audit.infrastructure.persistence.entity.AuditLogEntryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogEntryMapper {

    public AuditLogEntryJpaEntity toEntity(AuditLogEntry e) {
        return AuditLogEntryJpaEntity.builder()
            .id(e.getId().getValue())
            .sequenceNumber(e.getSequenceNumber())
            .category(e.getCategory())
            .sourceEventType(e.getSourceEventType())
            .sourceEventId(e.getSourceEventId())
            .subjectId(e.getSubjectId())
            .userId(e.getUserId())
            .summary(e.getSummary())
            .previousHash(e.getPreviousHash().getValue())
            .contentHash(e.getContentHash().getValue())
            .occurredAt(e.getOccurredAt())
            .recordedAt(e.getRecordedAt())
            .build();
    }

    public AuditLogEntry toDomain(AuditLogEntryJpaEntity e) {
        return AuditLogEntry.reconstitute(
            AuditLogId.of(e.getId()), e.getSequenceNumber(), e.getCategory(),
            e.getSourceEventType(), e.getSourceEventId(), e.getSubjectId(), e.getUserId(),
            e.getSummary(), ChainHash.of(e.getPreviousHash()), ChainHash.of(e.getContentHash()),
            e.getOccurredAt(), e.getRecordedAt());
    }
}
