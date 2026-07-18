package com.travel.booking.domain.aggregate;

import com.travel.booking.domain.event.*;
import com.travel.booking.domain.valueobject.*;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Booking Aggregate Root — the heart of the platform.
 *
 * Models the complete reservation lifecycle and acts as the saga
 * state machine for the distributed booking transaction.
 *
 * State machine (happy path):
 *   INITIATED → INVENTORY_RESERVED → PAYMENT_PENDING → CONFIRMED → COMPLETED
 *
 * Compensation paths:
 *   INITIATED       --[inventory fail]--> INVENTORY_FAILED → CANCELLED
 *   PAYMENT_PENDING --[payment fail]-->   PAYMENT_FAILED
 *                                         → INVENTORY_RELEASING
 *                                         → CANCELLED
 *
 * Rules:
 * - All state transitions are enforced here — no service can bypass them.
 * - Every state change that the saga needs to react to raises a domain event.
 * - The aggregate never calls other services — it only mutates state and
 *   raises events. Services react via Kafka consumers.
 */
public class Booking extends AggregateRoot<BookingId> {

    private final String      userId;
    private final BookingType bookingType;
    private final String      resourceId;
    private final String      resourceName;
    private BookingStatus     status;
    private final LocalDate   checkInDate;
    private final LocalDate   checkOutDate;
    private final int         guestCount;
    private final Money       totalAmount;
    private String            paymentId;
    private String            cancellationReason;
    private final Instant     createdAt;
    private Instant           updatedAt;

    // ── Private constructor — use factory methods ─────────────────────────────

    private Booking(BookingId id, String userId, BookingType bookingType,
                    String resourceId, String resourceName,
                    LocalDate checkInDate, LocalDate checkOutDate,
                    int guestCount, Money totalAmount) {
        super(id);
        this.userId       = userId;
        this.bookingType  = bookingType;
        this.resourceId   = resourceId;
        this.resourceName = resourceName;
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount   = guestCount;
        this.totalAmount  = totalAmount;
        this.status       = BookingStatus.INITIATED;
        this.createdAt    = Instant.now();
        this.updatedAt    = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Creates a new booking and starts the saga by raising BookingCreatedEvent.
     * Inventory services consume this event to place a resource hold.
     */
    public static Booking create(String userId, BookingType bookingType,
                                 String resourceId, String resourceName,
                                 LocalDate checkInDate, LocalDate checkOutDate,
                                 int guestCount, Money totalAmount) {
        validateDates(checkInDate, checkOutDate);
        if (guestCount < 1)
            throw new BusinessRuleViolationException(
                "Guest count must be at least 1", "INVALID_GUEST_COUNT");

        BookingId id      = BookingId.generate();
        Booking   booking = new Booking(id, userId, bookingType, resourceId, resourceName,
            checkInDate, checkOutDate, guestCount, totalAmount);

        booking.registerEvent(new BookingCreatedEvent(
            id.getValue(), userId, bookingType.name(), resourceId,
            checkInDate, checkOutDate, guestCount, totalAmount));

        return booking;
    }

    /**
     * Reconstitutes a Booking from persistence. No events raised.
     */
    public static Booking reconstitute(
        BookingId id, String userId, BookingType bookingType,
        String resourceId, String resourceName, BookingStatus status,
        LocalDate checkInDate, LocalDate checkOutDate,
        int guestCount, Money totalAmount,
        String paymentId, String cancellationReason,
        Instant createdAt, Instant updatedAt) {
        Booking b = new Booking(id, userId, bookingType, resourceId, resourceName,
            checkInDate, checkOutDate, guestCount, totalAmount);
        b.status             = status;
        b.paymentId          = paymentId;
        b.cancellationReason = cancellationReason;
        return b;
    }

    // ── Saga state transitions ────────────────────────────────────────────────

    /**
     * Saga step 1 success — inventory hold placed.
     * Raises InventoryReservedEvent → payment-service charges the card.
     */
    public void markInventoryReserved() {
        assertStatus(BookingStatus.INITIATED, "inventory reservation");
        this.status    = BookingStatus.INVENTORY_RESERVED;
        this.updatedAt = Instant.now();
        registerEvent(new InventoryReservedEvent(
            getId().getValue(), userId, totalAmount));
    }

    /**
     * Saga step 1 failure — resource unavailable.
     * Raises BookingCancelledEvent → notification-service sends failure email.
     */
    public void markInventoryUnavailable(String reason) {
        assertStatus(BookingStatus.INITIATED, "inventory failure");
        this.status             = BookingStatus.INVENTORY_FAILED;
        this.cancellationReason = reason;
        this.updatedAt          = Instant.now();
        registerEvent(new BookingCancelledEvent(
            getId().getValue(), userId, reason));
    }

    /**
     * Saga step 2 start — payment initiated.
     * Records the payment ID for correlation. No event needed here —
     * we wait for PaymentCompleted or PaymentFailed from payment-service.
     */
    public void markPaymentPending(String paymentId) {
        assertStatus(BookingStatus.INVENTORY_RESERVED, "payment initiation");
        this.paymentId = paymentId;
        this.status    = BookingStatus.PAYMENT_PENDING;
        this.updatedAt = Instant.now();
    }

    /**
     * Saga step 2 success — payment charged.
     * Raises BookingConfirmedEvent → notification, loyalty, analytics, audit.
     */
    public void confirmPayment() {
        assertStatus(BookingStatus.PAYMENT_PENDING, "payment confirmation");
        this.status    = BookingStatus.CONFIRMED;
        this.updatedAt = Instant.now();
        registerEvent(new BookingConfirmedEvent(
            getId().getValue(), userId, paymentId, totalAmount));
    }

    /**
     * Saga step 2 failure — payment declined.
     * Raises PaymentFailedEvent → inventory services release the hold
     * (compensating transaction).
     */
    public void markPaymentFailed(String reason) {
        assertStatus(BookingStatus.PAYMENT_PENDING, "payment failure");
        this.status             = BookingStatus.PAYMENT_FAILED;
        this.cancellationReason = reason;
        this.updatedAt          = Instant.now();
        registerEvent(new PaymentFailedEvent(
            getId().getValue(), userId, resourceId, bookingType.name(), reason));
    }

    /**
     * Compensating transaction in progress — inventory releasing.
     */
    public void markInventoryReleasing() {
        assertStatus(BookingStatus.PAYMENT_FAILED, "inventory release");
        this.status    = BookingStatus.INVENTORY_RELEASING;
        this.updatedAt = Instant.now();
    }

    /**
     * Compensating transaction complete — inventory released.
     * Raises BookingCancelledEvent → notification-service sends failure email.
     */
    public void markInventoryReleased() {
        if (status != BookingStatus.INVENTORY_RELEASING)
            throw new BusinessRuleViolationException(
                "Can only release from INVENTORY_RELEASING state",
                "INVALID_STATUS_TRANSITION");
        this.status    = BookingStatus.CANCELLED;
        this.updatedAt = Instant.now();
        registerEvent(new BookingCancelledEvent(
            getId().getValue(), userId, cancellationReason));
    }

    /**
     * User-initiated cancellation (only allowed before COMPLETED).
     */
    public void cancel(String reason) {
        if (status == BookingStatus.COMPLETED || status == BookingStatus.CANCELLED)
            throw new BusinessRuleViolationException(
                "Cannot cancel a " + status.name().toLowerCase() + " booking",
                "INVALID_STATUS_TRANSITION");
        this.cancellationReason = reason;
        this.status             = BookingStatus.CANCELLED;
        this.updatedAt          = Instant.now();
        registerEvent(new BookingCancelledEvent(
            getId().getValue(), userId, reason));
    }

    /**
     * Guest has checked out — booking is complete.
     * Raises BookingCompletedEvent → review-service, loyalty-service.
     */
    public void complete() {
        assertStatus(BookingStatus.CONFIRMED, "completion");
        this.status    = BookingStatus.COMPLETED;
        this.updatedAt = Instant.now();
        registerEvent(new BookingCompletedEvent(
            getId().getValue(), userId, resourceId));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void assertStatus(BookingStatus expected, String operation) {
        if (status != expected)
            throw new BusinessRuleViolationException(
                "Cannot perform [" + operation + "] when booking is in [" + status.name() + "] state",
                "INVALID_STATUS_TRANSITION");
    }

    private static void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null)
            throw new BusinessRuleViolationException(
                "Check-in and check-out dates are required", "INVALID_DATES");
        if (!checkIn.isBefore(checkOut))
            throw new BusinessRuleViolationException(
                "Check-in must be before check-out", "INVALID_DATES");
        if (checkIn.isBefore(LocalDate.now()))
            throw new BusinessRuleViolationException(
                "Check-in date cannot be in the past", "INVALID_DATES");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String      getUserId()            { return userId; }
    public BookingType getBookingType()       { return bookingType; }
    public String      getResourceId()        { return resourceId; }
    public String      getResourceName()      { return resourceName; }
    public BookingStatus getStatus()          { return status; }
    public LocalDate   getCheckInDate()       { return checkInDate; }
    public LocalDate   getCheckOutDate()      { return checkOutDate; }
    public int         getGuestCount()        { return guestCount; }
    public Money       getTotalAmount()       { return totalAmount; }
    public String      getPaymentId()         { return paymentId; }
    public String      getCancellationReason(){ return cancellationReason; }
    public Instant     getCreatedAt()         { return createdAt; }
    public Instant     getUpdatedAt()         { return updatedAt; }
}
