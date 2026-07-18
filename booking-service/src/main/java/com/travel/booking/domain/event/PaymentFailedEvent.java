package com.travel.booking.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when payment fails.
 * Consumed by inventory services as the signal to release the hold
 * (compensating transaction).
 */
public class PaymentFailedEvent extends DomainEvent {

    private final String bookingId;
    private final String userId;
    private final String resourceId;
    private final String bookingType;
    private final String reason;

    public PaymentFailedEvent(String bookingId, String userId,
                              String resourceId, String bookingType, String reason) {
        super("PaymentFailed");
        this.bookingId   = bookingId;
        this.userId      = userId;
        this.resourceId  = resourceId;
        this.bookingType = bookingType;
        this.reason      = reason;
    }

    @Override public String getAggregateId() { return bookingId; }
    public String getBookingId()  { return bookingId; }
    public String getUserId()     { return userId; }
    public String getResourceId() { return resourceId; }
    public String getBookingType(){ return bookingType; }
    public String getReason()     { return reason; }
}
