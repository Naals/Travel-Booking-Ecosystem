package com.travel.property.domain.aggregate;

import com.travel.property.domain.valueobject.DateRange;
import java.util.Objects;

/**
 * A booking hold on a property for a specific date range.
 * Stored as a collection on the Property aggregate — not a separate aggregate.
 * A Reservation is a pending hold (INVENTORY_RESERVED saga state) —
 * it becomes permanent when the booking is CONFIRMED.
 */
public final class Reservation {

    private final String    bookingId;
    private final String    userId;
    private final DateRange dateRange;
    private boolean         confirmed;

    public Reservation(String bookingId, String userId, DateRange dateRange) {
        this.bookingId = Objects.requireNonNull(bookingId);
        this.userId    = Objects.requireNonNull(userId);
        this.dateRange = Objects.requireNonNull(dateRange);
        this.confirmed = false;
    }

    public void confirm()  { this.confirmed = true; }

    public String    getBookingId() { return bookingId; }
    public String    getUserId()    { return userId; }
    public DateRange getDateRange() { return dateRange; }
    public boolean   isConfirmed()  { return confirmed; }
}
