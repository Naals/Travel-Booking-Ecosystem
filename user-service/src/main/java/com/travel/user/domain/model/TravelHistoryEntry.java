package com.travel.user.domain.model;

import java.time.Instant;

/**
 * A single completed trip, projected from booking-service's
 * BookingCompletedEvent into this service's own read model.
 *
 * Not a DDD Entity or ValueObject — a CQRS-style projection with no
 * invariants beyond basic construction validity and no behavior. The
 * authoritative record of the booking lives in booking-service; this
 * is a fast, locally-queryable copy for rendering travel history
 * without a cross-service call.
 */
public final class TravelHistoryEntry {

    private final String  userId;
    private final String  bookingId;
    private final String  resourceType;  // PROPERTY | HOTEL | FLIGHT | VEHICLE
    private final String  resourceName;
    private final Instant completedAt;

    private TravelHistoryEntry(String userId, String bookingId, String resourceType,
                               String resourceName, Instant completedAt) {
        this.userId       = userId;
        this.bookingId    = bookingId;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.completedAt  = completedAt;
    }

    public static TravelHistoryEntry of(String userId, String bookingId,
                                        String resourceType, String resourceName,
                                        Instant completedAt) {
        return new TravelHistoryEntry(userId, bookingId, resourceType, resourceName, completedAt);
    }

    public String  getUserId()       { return userId; }
    public String  getBookingId()    { return bookingId; }
    public String  getResourceType() { return resourceType; }
    public String  getResourceName() { return resourceName; }
    public Instant getCompletedAt()  { return completedAt; }
}
