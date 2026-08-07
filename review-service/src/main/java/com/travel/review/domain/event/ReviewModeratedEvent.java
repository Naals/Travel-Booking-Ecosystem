package com.travel.review.domain.event;

import com.travel.shared.event.DomainEvent;

public class ReviewModeratedEvent extends DomainEvent {

    private final String reviewId;
    private final String bookingId;
    private final String userId;
    private final String decision;
    private final String moderatorId;

    public ReviewModeratedEvent(String reviewId, String bookingId, String userId,
                                String decision, String moderatorId) {
        super("ReviewModerated");
        this.reviewId    = reviewId;
        this.bookingId   = bookingId;
        this.userId      = userId;
        this.decision    = decision;
        this.moderatorId = moderatorId;
    }

    @Override public String getAggregateId() { return reviewId; }
    public String getReviewId()    { return reviewId; }
    public String getBookingId()   { return bookingId; }
    public String getUserId()      { return userId; }
    public String getDecision()    { return decision; }
    public String getModeratorId() { return moderatorId; }
}
