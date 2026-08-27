package com.travel.analytics.domain.model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Platform-wide booking funnel counts for one (date, bookingType)
 * bucket. Not an aggregate root — no state machine, no domain events
 * to raise, since nothing downstream needs to react to "a metric
 * changed." Same modeling choice as UserAffinity and
 * DestinationPopularity (recommendation-service, Day 20), whose
 * Javadoc used this exact reasoning.
 *
 * Only increment methods are exposed — there is no decrement
 * operation, so unlike Wallet.debit() (Day 18) or
 * LoyaltyAccount.redeemPoints() (Day 19), no negative-value guard is
 * needed here; it's structurally impossible from this API surface.
 */
public final class DailyBookingMetric {

    private final LocalDate   date;
    private final BookingType bookingType;
    private long              createdCount;
    private long              confirmedCount;
    private long              completedCount;
    private long              cancelledCount;
    private Instant           updatedAt;

    private DailyBookingMetric(LocalDate date, BookingType bookingType,
                               long createdCount, long confirmedCount,
                               long completedCount, long cancelledCount,
                               Instant updatedAt) {
        this.date           = date;
        this.bookingType    = bookingType;
        this.createdCount   = createdCount;
        this.confirmedCount = confirmedCount;
        this.completedCount = completedCount;
        this.cancelledCount = cancelledCount;
        this.updatedAt      = updatedAt;
    }

    public static DailyBookingMetric initial(LocalDate date, BookingType bookingType) {
        return new DailyBookingMetric(date, bookingType, 0, 0, 0, 0, Instant.now());
    }

    public static DailyBookingMetric reconstitute(LocalDate date, BookingType bookingType,
                                                  long createdCount, long confirmedCount,
                                                  long completedCount, long cancelledCount,
                                                  Instant updatedAt) {
        return new DailyBookingMetric(date, bookingType, createdCount, confirmedCount,
            completedCount, cancelledCount, updatedAt);
    }

    public void incrementCreated()   { createdCount++;   updatedAt = Instant.now(); }
    public void incrementConfirmed() { confirmedCount++; updatedAt = Instant.now(); }
    public void incrementCompleted() { completedCount++; updatedAt = Instant.now(); }
    public void incrementCancelled() { cancelledCount++; updatedAt = Instant.now(); }

    public LocalDate   getDate()           { return date; }
    public BookingType getBookingType()    { return bookingType; }
    public long        getCreatedCount()   { return createdCount; }
    public long        getConfirmedCount() { return confirmedCount; }
    public long        getCompletedCount() { return completedCount; }
    public long        getCancelledCount() { return cancelledCount; }
    public Instant     getUpdatedAt()      { return updatedAt; }
}
