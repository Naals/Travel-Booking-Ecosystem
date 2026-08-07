package com.travel.review.domain.model;

import java.time.Instant;

/**
 * Local record of a completed booking, projected from booking-service's
 * BookingCompletedEvent. Existence of a matching, unconsumed record is
 * what makes a booking "reviewable" — review-service has no synchronous
 * dependency on booking-service to verify this at review-creation time.
 * See ReviewEligibilityRepositoryAdapter.tryConsume() for how the
 * "unconsumed" part is enforced atomically.
 */
public final class ReviewEligibility {

    private final String               bookingId;
    private final String               userId;
    private final String               resourceId;
    private final ReviewedResourceType resourceType;
    private final Instant              completedAt;

    private ReviewEligibility(String bookingId, String userId, String resourceId,
                              ReviewedResourceType resourceType, Instant completedAt) {
        this.bookingId    = bookingId;
        this.userId       = userId;
        this.resourceId   = resourceId;
        this.resourceType = resourceType;
        this.completedAt  = completedAt;
    }

    public static ReviewEligibility of(String bookingId, String userId, String resourceId,
                                       ReviewedResourceType resourceType, Instant completedAt) {
        return new ReviewEligibility(bookingId, userId, resourceId, resourceType, completedAt);
    }

    public String               getBookingId()    { return bookingId; }
    public String               getUserId()       { return userId; }
    public String               getResourceId()   { return resourceId; }
    public ReviewedResourceType getResourceType() { return resourceType; }
    public Instant              getCompletedAt()  { return completedAt; }
}
