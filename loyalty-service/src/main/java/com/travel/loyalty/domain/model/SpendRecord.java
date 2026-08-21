package com.travel.loyalty.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Bridges booking.booking-confirmed and booking.booking-completed.
 *
 * BookingConfirmedEvent (booking-service, Day 7) carries the amount
 * that was charged but arrives too early to trust for points-awarding —
 * a CONFIRMED booking can still be cancelled by the guest before
 * check-in (Booking.cancel() has no status restriction that rules this
 * out). BookingCompletedEvent (enriched Day 15) confirms the stay
 * genuinely happened, but carries no amount.
 *
 * onBookingConfirmed writes a SpendRecord (this class) keyed by
 * bookingId, holding the amount. onBookingCompleted atomically consumes
 * it and awards points. onBookingCancelled voids it outright so a
 * cancelled-after-confirmation booking can never later be consumed —
 * see BookingEventConsumer's three handlers.
 *
 * The same "eligibility record consumed by a later event" shape as
 * ReviewEligibility (review-service, Day 16), reimplemented here
 * against PostgreSQL instead of MongoDB — see SpendRecordRepository
 * .tryConsume() for the relational equivalent of Mongo's findAndModify.
 */
public final class SpendRecord {

    private final String     bookingId;
    private final String     userId;
    private final BigDecimal amount;
    private final String     currency;
    private final Instant    recordedAt;

    private SpendRecord(String bookingId, String userId, BigDecimal amount,
                        String currency, Instant recordedAt) {
        this.bookingId  = bookingId;
        this.userId     = userId;
        this.amount     = amount;
        this.currency   = currency;
        this.recordedAt = recordedAt;
    }

    public static SpendRecord of(String bookingId, String userId,
                                 BigDecimal amount, String currency) {
        return new SpendRecord(bookingId, userId, amount, currency, Instant.now());
    }

    public String     getBookingId()  { return bookingId; }
    public String     getUserId()     { return userId; }
    public BigDecimal getAmount()     { return amount; }
    public String     getCurrency()   { return currency; }
    public Instant     getRecordedAt() { return recordedAt; }
}
