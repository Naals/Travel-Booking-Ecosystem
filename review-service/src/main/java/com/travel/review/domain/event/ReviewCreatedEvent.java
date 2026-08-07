package com.travel.review.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * First real producer of KafkaTopics.REVIEW_CREATED — that constant
 * was declared in common-lib back on Day 3, ahead of any service
 * that would actually publish to it.
 */
public class ReviewCreatedEvent extends DomainEvent {

    private final String reviewId;
    private final String bookingId;
    private final String userId;
    private final String resourceId;
    private final String resourceType;
    private final int    rating;
    private final String initialStatus;

    public ReviewCreatedEvent(String reviewId, String bookingId, String userId,
                              String resourceId, String resourceType,
                              int rating, String initialStatus) {
        super("ReviewCreated");
        this.reviewId      = reviewId;
        this.bookingId     = bookingId;
        this.userId        = userId;
        this.resourceId    = resourceId;
        this.resourceType  = resourceType;
        this.rating        = rating;
        this.initialStatus = initialStatus;
    }

    @Override public String getAggregateId() { return reviewId; }
    public String getReviewId()      { return reviewId; }
    public String getBookingId()     { return bookingId; }
    public String getUserId()        { return userId; }
    public String getResourceId()    { return resourceId; }
    public String getResourceType()  { return resourceType; }
    public int    getRating()        { return rating; }
    public String getInitialStatus() { return initialStatus; }
}
