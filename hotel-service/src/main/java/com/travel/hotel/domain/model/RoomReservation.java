package com.travel.hotel.domain.model;

import com.travel.hotel.domain.valueobject.DateRange;
import java.util.Objects;

/**
 * A booking hold on a specific room for a date range.
 * Owned by the Room entity — not an independent aggregate.
 */
public final class RoomReservation {

    private final String    bookingId;
    private final String    userId;
    private final DateRange dateRange;
    private boolean         confirmed;

    public RoomReservation(String bookingId, String userId, DateRange dateRange) {
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
