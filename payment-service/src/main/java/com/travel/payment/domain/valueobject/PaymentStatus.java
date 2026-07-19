package com.travel.payment.domain.valueobject;

/**
 * Payment lifecycle states.
 *
 * Happy path:  PENDING → PROCESSING → COMPLETED
 * Failure:     PENDING → FAILED
 *              PROCESSING → FAILED
 * Refund:      COMPLETED → REFUND_REQUESTED → REFUNDED
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUND_REQUESTED,
    REFUNDED
}
