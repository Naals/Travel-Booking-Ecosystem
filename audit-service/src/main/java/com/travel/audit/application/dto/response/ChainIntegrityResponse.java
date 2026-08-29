package com.travel.audit.application.dto.response;

import com.travel.audit.domain.model.ChainIntegrityReport;

public record ChainIntegrityResponse(
    boolean valid,
    long    entriesChecked,
    Long    brokenAtSequence,
    String  failureReason
) {
    public static ChainIntegrityResponse from(ChainIntegrityReport r) {
        return new ChainIntegrityResponse(r.valid(), r.entriesChecked(), r.brokenAtSequence(), r.failureReason());
    }
}
