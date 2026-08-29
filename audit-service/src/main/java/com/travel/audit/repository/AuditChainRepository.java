package com.travel.audit.domain.repository;

import com.travel.audit.domain.model.ChainPosition;
import com.travel.audit.domain.valueobject.ChainHash;

/**
 * lockHeadForAppend() must be called inside an active @Transactional
 * boundary — its row-level lock is held only for the lifetime of that
 * transaction. Every implementation of this port must NOT declare its
 * own @Transactional (a REQUIRES_NEW propagation would use a separate
 * connection and release the lock immediately, defeating its purpose
 * entirely); the lock's lifetime is controlled by the caller
 * (RecordAuditEntryUseCase) alone. See ADR-015.
 */
public interface AuditChainRepository {
    ChainPosition lockHeadForAppend();
    void          advanceHead(long newSequenceNumber, ChainHash newHash);
}
