package com.travel.booking.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a guest checks out and the stay is complete.
 * Consumed by: review-service (triggers review request),
 *              loyalty-service, analytics-service,
 *              user-service (projects into travel history — Day 15).
 *
 * bookingType and resourceName were added on Day 15 when user-service's
 * travel-history projection needed them — both were already available
 * on the aggregate at the call site, so this was a same-day addition
 * rather than a redesign.
 */
public class BookingCompletedEvent extends DomainEvent {

    private final String bookingId;
    private final String userId;
    private final String resourceId;
    private final String bookingType;
    private final String resourceName;

    public BookingCompletedEvent(String bookingId, String userId, String resourceId,
                                 String bookingType, String resourceName) {
        super("BookingCompleted");
        this.bookingId    = bookingId;
        this.userId       = userId;
        this.resourceId   = resourceId;
        this.bookingType  = bookingType;
        this.resourceName = resourceName;
    }

    @Override public String getAggregateId() { return bookingId; }
    public String getBookingId()    { return bookingId; }
    public String getUserId()       { return userId; }
    public String getResourceId()   { return resourceId; }
    public String getBookingType()  { return bookingType; }
    public String getResourceName() { return resourceName; }
}
