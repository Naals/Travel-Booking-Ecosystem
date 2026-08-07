package com.travel.review.domain.model;

import java.time.Instant;

/**
 * Aggregate rating statistics for a single resource, recomputed from
 * scratch (not incrementally maintained) whenever a
 * ResourceRatingUpdatedEvent fires for that resource — see
 * RatingAggregationService. Recomputing avoids drift from partial
 * updates (e.g. a moderator rejecting a previously-approved review)
 * at the cost of a full re-scan per update, acceptable at the
 * per-resource review volumes this platform expects.
 */
public final class RatingSummary {

    private final String               resourceId;
    private final ReviewedResourceType resourceType;
    private final double               averageRating;
    private final long                 reviewCount;
    private final Instant              updatedAt;

    private RatingSummary(String resourceId, ReviewedResourceType resourceType,
                          double averageRating, long reviewCount, Instant updatedAt) {
        this.resourceId    = resourceId;
        this.resourceType  = resourceType;
        this.averageRating = averageRating;
        this.reviewCount   = reviewCount;
        this.updatedAt     = updatedAt;
    }

    public static RatingSummary of(String resourceId, ReviewedResourceType resourceType,
                                   double averageRating, long reviewCount) {
        return new RatingSummary(resourceId, resourceType, averageRating, reviewCount, Instant.now());
    }

    public String               getResourceId()    { return resourceId; }
    public ReviewedResourceType getResourceType()  { return resourceType; }
    public double                getAverageRating() { return averageRating; }
    public long                  getReviewCount()   { return reviewCount; }
    public Instant               getUpdatedAt()     { return updatedAt; }
}
