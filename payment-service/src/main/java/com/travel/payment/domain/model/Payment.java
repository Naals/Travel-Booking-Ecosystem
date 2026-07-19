package com.travel.payment.domain.model;

import com.travel.payment.domain.event.*;
import com.travel.payment.domain.valueobject.*;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;

/**
 * Payment Aggregate Root.
 *
 * Tracks the full lifecycle of a single payment transaction:
 *   PENDING → PROCESSING → COMPLETED
 *   PENDING/PROCESSING → FAILED
 *   COMPLETED → REFUND_REQUESTED → REFUNDED
 *
 * Design decisions:
 * - One Payment aggregate per booking. If a payment fails and the user
 *   retries, a new Payment aggregate is created — old one remains as
 *   a historical record.
 * - externalPaymentId is the Stripe PaymentIntent ID — used for
 *   correlation and refund initiation.
 * - idempotencyKey is a client-supplied key ensuring the same charge
 *   is never applied twice even if the consumer processes the event twice.
 */
public class Payment extends AggregateRoot<PaymentId> {

    private final String        bookingId;
    private final String        userId;
    private final Money         amount;
    private PaymentStatus       status;
    private PaymentMethod       paymentMethod;
    private String              externalPaymentId;
    private String              idempotencyKey;
    private String              failureReason;
    private String              refundId;
    private final Instant       createdAt;
    private Instant             updatedAt;

    // ── Private constructor ───────────────────────────────────────────────────

    private Payment(PaymentId id, String bookingId, String userId,
                    Money amount, PaymentMethod method, String idempotencyKey) {
        super(id);
        this.bookingId       = bookingId;
        this.userId          = userId;
        this.amount          = amount;
        this.paymentMethod   = method;
        this.idempotencyKey  = idempotencyKey;
        this.status          = PaymentStatus.PENDING;
        this.createdAt       = Instant.now();
        this.updatedAt       = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Initiates a new payment.
     * idempotencyKey = bookingId + "-" + attempt number — ensures Stripe
     * never charges twice for the same booking attempt.
     */
    public static Payment initiate(String bookingId, String userId,
                                   Money amount, PaymentMethod method) {
        PaymentId id             = PaymentId.generate();
        String    idempotencyKey = bookingId + "-" + id.getValue();
        Payment   payment        = new Payment(id, bookingId, userId, amount,
            method, idempotencyKey);
        payment.registerEvent(new PaymentInitiatedEvent(
            id.getValue(), bookingId, userId, amount));
        return payment;
    }

    public static Payment reconstitute(
        PaymentId id, String bookingId, String userId,
        Money amount, PaymentStatus status, PaymentMethod method,
        String externalPaymentId, String idempotencyKey,
        String failureReason, String refundId,
        Instant createdAt, Instant updatedAt) {
        Payment p = new Payment(id, bookingId, userId, amount, method, idempotencyKey);
        p.status            = status;
        p.externalPaymentId = externalPaymentId;
        p.failureReason     = failureReason;
        p.refundId          = refundId;
        return p;
    }

    // ── State transitions ─────────────────────────────────────────────────────

    /**
     * Stripe PaymentIntent created — charge in progress.
     */
    public void markProcessing(String externalPaymentId) {
        assertStatus(PaymentStatus.PENDING, "mark processing");
        this.externalPaymentId = externalPaymentId;
        this.status            = PaymentStatus.PROCESSING;
        this.updatedAt         = Instant.now();
    }

    /**
     * Stripe confirms charge succeeded.
     * Raises PaymentCompletedEvent → booking-service advances saga.
     */
    public void complete() {
        assertStatus(PaymentStatus.PROCESSING, "complete");
        this.status    = PaymentStatus.COMPLETED;
        this.updatedAt = Instant.now();
        registerEvent(new PaymentCompletedEvent(
            getId().getValue(), bookingId, userId, amount, externalPaymentId));
    }

    /**
     * Charge failed (Stripe decline, insufficient funds, etc).
     * Raises PaymentFailedEvent → booking-service triggers compensation.
     */
    public void fail(String reason) {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING)
            throw new BusinessRuleViolationException(
                "Cannot fail a payment in status: " + status, "INVALID_STATUS_TRANSITION");
        this.failureReason = reason;
        this.status        = PaymentStatus.FAILED;
        this.updatedAt     = Instant.now();
        registerEvent(new PaymentFailedEvent(
            getId().getValue(), bookingId, userId, reason));
    }

    /**
     * Refund requested (e.g. booking cancelled after payment).
     * Raises RefundInitiatedEvent — Stripe refund call follows in use case.
     */
    public void requestRefund() {
        if (status != PaymentStatus.COMPLETED)
            throw new BusinessRuleViolationException(
                "Can only refund a COMPLETED payment", "INVALID_STATUS_TRANSITION");
        this.status    = PaymentStatus.REFUND_REQUESTED;
        this.updatedAt = Instant.now();
        registerEvent(new RefundInitiatedEvent(
            getId().getValue(), bookingId, userId, amount, externalPaymentId));
    }

    /**
     * Stripe confirms refund processed.
     * Raises RefundCompletedEvent → notification, wallet, audit.
     */
    public void completeRefund(String refundId) {
        assertStatus(PaymentStatus.REFUND_REQUESTED, "complete refund");
        this.refundId  = refundId;
        this.status    = PaymentStatus.REFUNDED;
        this.updatedAt = Instant.now();
        registerEvent(new RefundCompletedEvent(
            getId().getValue(), bookingId, userId, amount, refundId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void assertStatus(PaymentStatus expected, String operation) {
        if (status != expected)
            throw new BusinessRuleViolationException(
                "Cannot [" + operation + "] when payment is in [" + status.name() + "] state",
                "INVALID_STATUS_TRANSITION");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String        getBookingId()        { return bookingId; }
    public String        getUserId()           { return userId; }
    public Money         getAmount()           { return amount; }
    public PaymentStatus getStatus()           { return status; }
    public PaymentMethod getPaymentMethod()    { return paymentMethod; }
    public String        getExternalPaymentId(){ return externalPaymentId; }
    public String        getIdempotencyKey()   { return idempotencyKey; }
    public String        getFailureReason()    { return failureReason; }
    public String        getRefundId()         { return refundId; }
    public Instant       getCreatedAt()        { return createdAt; }
    public Instant       getUpdatedAt()        { return updatedAt; }
}
