package com.travel.flight.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a seat reservation is released (compensation transaction).
 * Consumed by booking-service saga as inventory.reservation-released.
 */
public class SeatReservationReleasedEvent extends DomainEvent {

    private final String flightId;
    private final String seatId;
    private final String bookingId;
    private final String reason;

    public SeatReservationReleasedEvent(String flightId, String seatId,
                                        String bookingId, String reason) {
        super("SeatReservationReleased");
        this.flightId  = flightId;
        this.seatId    = seatId;
        this.bookingId = bookingId;
        this.reason    = reason;
    }

    @Override public String getAggregateId() { return flightId; }
    public String getFlightId()  { return flightId; }
    public String getSeatId()    { return seatId; }
    public String getBookingId() { return bookingId; }
    public String getReason()    { return reason; }
}
