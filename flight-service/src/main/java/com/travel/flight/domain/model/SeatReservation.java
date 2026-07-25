package com.travel.flight.domain.model;

import java.util.Objects;

/**
 * A booking hold on a specific seat.
 * Owned by the Seat entity — not an independent aggregate.
 * A seat can hold at most one reservation at a time.
 */
public final class SeatReservation {

    private final String  bookingId;
    private final String  userId;
    private       boolean confirmed;

    public SeatReservation(String bookingId, String userId) {
        this.bookingId = Objects.requireNonNull(bookingId);
        this.userId    = Objects.requireNonNull(userId);
        this.confirmed = false;
    }

    public void confirm()  { this.confirmed = true; }

    public String  getBookingId() { return bookingId; }
    public String  getUserId()    { return userId; }
    public boolean isConfirmed()  { return confirmed; }
}
