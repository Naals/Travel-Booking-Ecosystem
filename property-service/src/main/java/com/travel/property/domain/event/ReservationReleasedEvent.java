package com.travel.property.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a booking hold is released (compensation transaction).
 * Consumed by booking-service saga as inventory.reservation-released.
 */
public class ReservationReleasedEvent extends DomainEvent {

    private final String propertyId;
    private final String bookingId;
    private final String reason;

    public ReservationReleasedEvent(String propertyId, String bookingId, String reason) {
        super("ReservationReleased");
        this.propertyId = propertyId;
        this.bookingId  = bookingId;
        this.reason     = reason;
    }

    @Override public String getAggregateId() { return propertyId; }
    public String getPropertyId() { return propertyId; }
    public String getBookingId()  { return bookingId; }
    public String getReason()     { return reason; }
}
