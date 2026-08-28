package com.travel.audit.domain.model;

/** Result of walking the full chain — see ChainIntegrityVerifier. */
public record ChainIntegrityReport(
    boolean valid,
    long    entriesChecked,
    Long    brokenAtSequence,
    String  failureReason
) {
    public static ChainIntegrityReport valid(long entriesChecked) {
        return new ChainIntegrityReport(true, entriesChecked, null, null);
    }

    public static ChainIntegrityReport broken(long sequenceNumber, String reason) {
        return new ChainIntegrityReport(false, sequenceNumber - 1, sequenceNumber, reason);
    }
}
