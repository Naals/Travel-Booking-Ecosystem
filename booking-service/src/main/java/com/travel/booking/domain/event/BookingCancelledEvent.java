package com.travel.booking.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published on any cancellation path — user-initiated or saga compensation.
 * Consumed by: notification-service, analytics-service, audit-service.
 */
public class BookingCancelledEvent extends DomainEvent {

    private final String bookingId;
    private final String userId;
    private final String reason;

    public BookingCancelledEvent(String bookingId, String userId, String reason) {
        super("BookingCancelled");
        this.bookingId = bookingId;
        this.userId    = userId;
        this.reason    = reason;
    }

    @Override public String getAggregateId() { return bookingId; }
    public String getBookingId() { return bookingId; }
    public String getUserId()    { return userId; }
    public String getReason()    { return reason; }
}
