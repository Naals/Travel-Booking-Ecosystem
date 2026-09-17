package com.travel.booking.application.saga;

import com.travel.booking.domain.valueobject.BookingStatus;

/**
 * Thrown when a saga step is attempted before its precondition state
 * has been reached — NOT when it's already been applied (that's a
 * safe, idempotent skip, handled separately by early-return in
 * BookingSaga, never by throwing). Distinguishing the two matters: an
 * already-applied transition should be acknowledged and dropped; an
 * out-of-order one must NOT be, so Kafka retries it once the real
 * precondition event has actually landed. See ADR-016.
 *
 * Deliberately unchecked and does NOT extend BusinessRuleViolationException
 * — the two need to be caught, and handled, completely differently by
 * BookingSagaConsumer.
 */
public class SagaOutOfOrderException extends RuntimeException {

    private final String        bookingId;
    private final BookingStatus expectedStatus;
    private final BookingStatus actualStatus;

    public SagaOutOfOrderException(String bookingId, BookingStatus expectedStatus, BookingStatus actualStatus) {
        super("Booking " + bookingId + " expected status " + expectedStatus
            + " but was " + actualStatus + " — event arrived before its precondition was met");
        this.bookingId      = bookingId;
        this.expectedStatus = expectedStatus;
        this.actualStatus   = actualStatus;
    }

    public String        getBookingId()      { return bookingId; }
    public BookingStatus getExpectedStatus() { return expectedStatus; }
    public BookingStatus getActualStatus()   { return actualStatus; }
}
