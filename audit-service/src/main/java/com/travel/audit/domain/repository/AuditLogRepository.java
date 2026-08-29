package com.travel.audit.domain.repository;

import com.travel.audit.domain.model.AuditLogEntry;

import java.util.List;

public interface AuditLogRepository {
    void    insert(AuditLogEntry entry);
    boolean existsBySourceEventId(String sourceEventId);

    List<AuditLogEntry> findBySubjectId(String subjectId, int page, int size);
    long                 countBySubjectId(String subjectId);

    List<AuditLogEntry> findByUserId(String userId, int page, int size);
    long                 countByUserId(String userId);

    /**
     * Full, unpaginated scan in chain order — required for integrity
     * verification, which is only meaningful end-to-end (a tampered
     * entry anywhere invalidates every hash after it, so checking a
     * subset proves nothing). See ADR-015 for the scale tradeoff this
     * implies and how a production system would evolve it.
     */
    List<AuditLogEntry> findAllOrderedBySequence();
}
