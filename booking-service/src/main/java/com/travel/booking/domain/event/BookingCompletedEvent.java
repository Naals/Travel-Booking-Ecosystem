package com.travel.booking.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a guest checks out and the stay is complete.
 * Consumed by: review-service (triggers review request),
 *              loyalty-service, analytics-service.
 */
public class BookingCompletedEvent extends DomainEvent {

    private final String bookingId;
    private final String userId;
    private final String resourceId;

    public BookingCompletedEvent(String bookingId, String userId, String resourceId) {
        super("BookingCompleted");
        this.bookingId  = bookingId;
        this.userId     = userId;
        this.resourceId = resourceId;
    }

    @Override public String getAggregateId() { return bookingId; }
    public String getBookingId()  { return bookingId; }
    public String getUserId()     { return userId; }
    public String getResourceId() { return resourceId; }
}
