package com.travel.review.domain.event;

import com.travel.shared.event.DomainEvent;

public class ReviewFlaggedEvent extends DomainEvent {

    private final String reviewId;
    private final String resourceId;
    private final String resourceType;
    private final String reporterId;
    private final String reason;

    public ReviewFlaggedEvent(String reviewId, String resourceId, String resourceType,
                              String reporterId, String reason) {
        super("ReviewFlagged");
        this.reviewId     = reviewId;
        this.resourceId   = resourceId;
        this.resourceType = resourceType;
        this.reporterId   = reporterId;
        this.reason       = reason;
    }

    @Override public String getAggregateId() { return reviewId; }
    public String getReviewId()     { return reviewId; }
    public String getResourceId()   { return resourceId; }
    public String getResourceType() { return resourceType; }
    public String getReporterId()   { return reporterId; }
    public String getReason()       { return reason; }
}
