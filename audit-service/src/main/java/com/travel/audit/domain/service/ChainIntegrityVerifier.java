package com.travel.audit.domain.service;

import com.travel.audit.domain.model.AuditLogEntry;
import com.travel.audit.domain.model.ChainIntegrityReport;
import com.travel.audit.domain.valueobject.ChainHash;

import java.util.List;

/**
 * Pure function: entries in sequence order in, a verdict out. No
 * repository, no Spring, fully unit-testable without mocks — the same
 * "domain service takes data, returns a decision" shape as
 * FraudRuleEngine (fraud-service, Day 21), just static rather than a
 * Spring bean since, unlike FraudRuleEngine's list of injected
 * FraudRule beans, there is nothing here for Spring to assemble.
 *
 * Two independent checks per entry: its declared previousHash must
 * equal the prior entry's actual contentHash (the link), and its own
 * contentHash must equal what HashChainService recomputes from its
 * stored fields (the content). Either mismatch means something in the
 * chain has been altered since it was written.
 */
public final class ChainIntegrityVerifier {

    private ChainIntegrityVerifier() {}

    public static ChainIntegrityReport verify(List<AuditLogEntry> entriesInSequenceOrder) {
        ChainHash expectedPrevious = ChainHash.GENESIS;

        for (AuditLogEntry entry : entriesInSequenceOrder) {
            if (!entry.getPreviousHash().equals(expectedPrevious)) {
                return ChainIntegrityReport.broken(entry.getSequenceNumber(),
                    "previousHash does not match the prior entry's contentHash");
            }

            ChainHash recomputed = HashChainService.computeHash(
                entry.getSequenceNumber(), entry.getCategory(), entry.getSourceEventType(),
                entry.getSourceEventId(), entry.getSubjectId(), entry.getSummary(),
                entry.getOccurredAt(), entry.getPreviousHash());

            if (!recomputed.equals(entry.getContentHash())) {
                return ChainIntegrityReport.broken(entry.getSequenceNumber(),
                    "contentHash does not match recomputed hash — entry content may have been altered");
            }

            expectedPrevious = entry.getContentHash();
        }

        return ChainIntegrityReport.valid(entriesInSequenceOrder.size());
    }
}
