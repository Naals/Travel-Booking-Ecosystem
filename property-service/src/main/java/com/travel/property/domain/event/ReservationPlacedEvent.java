package com.travel.property.domain.event;

import com.travel.shared.event.DomainEvent;
import java.time.LocalDate;

/**
 * Published when a booking hold is placed on this property.
 * Consumed by booking-service saga as inventory.reservation-confirmed.
 */
public class ReservationPlacedEvent extends DomainEvent {

    private final String    propertyId;
    private final String    bookingId;
    private final String    userId;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;

    public ReservationPlacedEvent(String propertyId, String bookingId,
                                  String userId, LocalDate checkInDate,
                                  LocalDate checkOutDate) {
        super("ReservationPlaced");
        this.propertyId   = propertyId;
        this.bookingId    = bookingId;
        this.userId       = userId;
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    @Override public String getAggregateId() { return propertyId; }
    public String    getPropertyId()  { return propertyId; }
    public String    getBookingId()   { return bookingId; }
    public String    getUserId()      { return userId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate(){ return checkOutDate; }
}
