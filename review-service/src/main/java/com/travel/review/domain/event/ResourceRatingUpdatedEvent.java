package com.travel.review.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Signals that resourceId's approved-review set changed and its
 * rating aggregate should be recomputed — see RatingRecomputeConsumer.
 *
 * getAggregateId() returns the reviewId that triggered the signal,
 * consistent with every event in this platform (the ID belongs to
 * the aggregate that called registerEvent()) — NOT resourceId, even
 * though resourceId is what a consumer actually cares about grouping
 * by. Consumers needing per-resource ordering should key off
 * getResourceId() from the payload rather than the Kafka partition key.
 */
public class ResourceRatingUpdatedEvent extends DomainEvent {

    private final String reviewId;
    private final String resourceId;
    private final String resourceType;

    public ResourceRatingUpdatedEvent(String reviewId, String resourceId, String resourceType) {
        super("ResourceRatingUpdated");
        this.reviewId     = reviewId;
        this.resourceId   = resourceId;
        this.resourceType = resourceType;
    }

    @Override public String getAggregateId() { return reviewId; }
    public String getReviewId()     { return reviewId; }
    public String getResourceId()   { return resourceId; }
    public String getResourceType() { return resourceType; }
}
