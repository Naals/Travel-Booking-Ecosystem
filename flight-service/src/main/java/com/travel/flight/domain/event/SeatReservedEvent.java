package com.travel.flight.domain.event;

import com.travel.flight.domain.valueobject.Money;
import com.travel.shared.event.DomainEvent;

/**
 * Published when a seat is reserved for a booking.
 * Consumed by booking-service saga as inventory.reservation-confirmed.
 */
public class SeatReservedEvent extends DomainEvent {

    private final String flightId;
    private final String seatId;
    private final String seatNumber;
    private final String seatClass;
    private final String bookingId;
    private final String userId;
    private final Money  price;

    public SeatReservedEvent(String flightId, String seatId, String seatNumber,
                             String seatClass, String bookingId,
                             String userId, Money price) {
        super("SeatReserved");
        this.flightId   = flightId;
        this.seatId     = seatId;
        this.seatNumber = seatNumber;
        this.seatClass  = seatClass;
        this.bookingId  = bookingId;
        this.userId     = userId;
        this.price      = price;
    }

    @Override public String getAggregateId() { return flightId; }
    public String getFlightId()  { return flightId; }
    public String getSeatId()    { return seatId; }
    public String getSeatNumber(){ return seatNumber; }
    public String getSeatClass() { return seatClass; }
    public String getBookingId() { return bookingId; }
    public String getUserId()    { return userId; }
    public Money  getPrice()     { return price; }
}
