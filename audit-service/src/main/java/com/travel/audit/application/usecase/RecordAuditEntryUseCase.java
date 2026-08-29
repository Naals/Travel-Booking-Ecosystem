package com.travel.audit.application.usecase;

import com.travel.audit.domain.event.AuditLogCreatedEvent;
import com.travel.audit.domain.model.AuditCategory;
import com.travel.audit.domain.model.AuditLogEntry;
import com.travel.audit.domain.model.ChainPosition;
import com.travel.audit.domain.repository.AuditChainRepository;
import com.travel.audit.domain.repository.AuditLogRepository;
import com.travel.audit.domain.service.HashChainService;
import com.travel.audit.domain.valueobject.AuditLogId;
import com.travel.audit.domain.valueobject.ChainHash;
import com.travel.audit.infrastructure.messaging.producer.AuditEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * The single write path for the entire chain — every Kafka consumer
 * in this service calls this and nothing else appends directly. That
 * centralization is not a style preference: computing a new entry's
 * hash requires knowing the current chain tail, and that can only be
 * determined safely if every writer serializes through the same
 * locked read (AuditChainRepository.lockHeadForAppend()).
 *
 * Unlike analytics-service's dedup check (ADR-014, Day 22), which
 * runs in a generic EventDeduplicationRepository BEFORE its use case
 * is invoked, the duplicate check here runs AFTER acquiring the
 * chain-head lock, inside this same transaction. "Is this a
 * duplicate" and "what position does this entry take in the chain"
 * are not independent questions the way they are for a plain counter
 * increment — both need the same lock, so there is no benefit to a
 * separate pre-check, and a real correctness risk if the duplicate
 * check ran unlocked while a concurrent append was mid-flight. See
 * ADR-015 for the full comparison with Day 22's approach.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordAuditEntryUseCase {

    private final AuditChainRepository chainRepository;
    private final AuditLogRepository   auditLogRepository;
    private final AuditEventPublisher  eventPublisher;

    @Transactional
    public void execute(AuditCategory category, String sourceEventType, String sourceEventId,
                        String subjectId, String userId, String summary, Instant occurredAt) {

        ChainPosition head = chainRepository.lockHeadForAppend();

        if (auditLogRepository.existsBySourceEventId(sourceEventId)) {
            log.debug("Duplicate delivery of {} (sourceEventId={}) — already recorded, skipping",
                sourceEventType, sourceEventId);
            return;
        }

        long nextSequence = head.sequenceNumber() + 1;
        ChainHash contentHash = HashChainService.computeHash(
            nextSequence, category, sourceEventType, sourceEventId,
            subjectId, summary, occurredAt, head.hash());

        AuditLogEntry entry = AuditLogEntry.create(
            AuditLogId.generate(), nextSequence, category, sourceEventType, sourceEventId,
            subjectId, userId, summary, head.hash(), contentHash, occurredAt, Instant.now());

        auditLogRepository.insert(entry);
        chainRepository.advanceHead(nextSequence, contentHash);

        eventPublisher.publish(new AuditLogCreatedEvent(
            entry.getId().getValue(), entry.getSequenceNumber(), entry.getCategory().name(),
            entry.getSourceEventType(), entry.getSubjectId()));

        log.debug("Audit entry recorded: seq={} category={} sourceEventType={}",
            nextSequence, category, sourceEventType);
    }
}
