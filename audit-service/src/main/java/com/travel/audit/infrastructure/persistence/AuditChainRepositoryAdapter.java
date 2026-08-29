package com.travel.audit.infrastructure.persistence;

import com.travel.audit.domain.model.ChainPosition;
import com.travel.audit.domain.repository.AuditChainRepository;
import com.travel.audit.domain.valueobject.ChainHash;
import com.travel.audit.infrastructure.persistence.repository.AuditChainHeadJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deliberately carries no @Transactional annotations of its own — see
 * AuditChainRepository's port Javadoc for why. Both methods here rely
 * entirely on running inside the transaction RecordAuditEntryUseCase
 * already started.
 */
@Component
@RequiredArgsConstructor
public class AuditChainRepositoryAdapter implements AuditChainRepository {

    /** Matches the row Flyway's V1 migration seeds. */
    private static final String SINGLETON_ID = "GLOBAL_CHAIN_HEAD";

    private final AuditChainHeadJpaRepository jpa;

    @Override
    public ChainPosition lockHeadForAppend() {
        var head = jpa.lockById(SINGLETON_ID)
            .orElseThrow(() -> new IllegalStateException(
                "audit_chain_head singleton row missing — Flyway V1 migration should have seeded it"));
        return new ChainPosition(head.getLastSequenceNumber(), ChainHash.of(head.getLastHash()));
    }

    @Override
    public void advanceHead(long newSequenceNumber, ChainHash newHash) {
        var head = jpa.lockById(SINGLETON_ID)
            .orElseThrow(() -> new IllegalStateException("audit_chain_head singleton row missing"));
        head.setLastSequenceNumber(newSequenceNumber);
        head.setLastHash(newHash.getValue());
        jpa.save(head);
    }
}
