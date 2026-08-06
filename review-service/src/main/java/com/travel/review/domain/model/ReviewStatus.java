package com.travel.review.domain.model;

/**
 * Moderation state machine:
 *   write() → PENDING_MODERATION (denylist hit) or APPROVED (clean)
 *   PENDING_MODERATION → APPROVED | REJECTED
 *   APPROVED           → FLAGGED (community report)
 *   FLAGGED            → APPROVED | REJECTED (re-moderation)
 */
public enum ReviewStatus {
    PENDING_MODERATION,
    APPROVED,
    REJECTED,
    FLAGGED
}
