package com.travel.review.domain.aggregate;

import com.travel.review.domain.event.*;
import com.travel.review.domain.model.ReviewStatus;
import com.travel.review.domain.model.ReviewedResourceType;
import com.travel.review.domain.valueobject.Rating;
import com.travel.review.domain.valueobject.ReviewContent;
import com.travel.review.domain.valueobject.ReviewId;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;

/**
 * Review Aggregate Root.
 *
 * One review per completed booking — enforced at construction time by
 * CreateReviewUseCase's atomic eligibility check, with a unique Mongo
 * index on bookingId (see V1 index annotations) as a backstop.
 *
 * Only APPROVED reviews contribute to a resource's rating aggregate.
 * approve() and reject() both raise ResourceRatingUpdatedEvent — reject
 * needs to as well, because a review can reach REJECTED after having
 * been APPROVED (APPROVED → FLAGGED → REJECTED), which must remove it
 * from the aggregate.
 */
public class Review extends AggregateRoot<ReviewId> {

    private final String                bookingId;
    private final String                userId;
    private final String                resourceId;
    private final ReviewedResourceType  resourceType;
    private final Rating                rating;
    private final ReviewContent         content;
    private ReviewStatus                status;
    private String                      moderatorId;
    private String                      moderationReason;
    private final Instant               createdAt;
    private Instant                     updatedAt;

    private Review(ReviewId id, String bookingId, String userId, String resourceId,
                   ReviewedResourceType resourceType, Rating rating, ReviewContent content) {
        super(id);
        this.bookingId    = bookingId;
        this.userId       = userId;
        this.resourceId   = resourceId;
        this.resourceType = resourceType;
        this.rating       = rating;
        this.content       = content;
        this.createdAt     = Instant.now();
        this.updatedAt      = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static Review write(String bookingId, String userId, String resourceId,
                               ReviewedResourceType resourceType, Rating rating,
                               ReviewContent content, boolean requiresManualReview) {
        ReviewId id     = ReviewId.generate();
        Review   review = new Review(id, bookingId, userId, resourceId, resourceType, rating, content);
        review.status   = requiresManualReview ? ReviewStatus.PENDING_MODERATION : ReviewStatus.APPROVED;

        review.registerEvent(new ReviewCreatedEvent(
            id.getValue(), bookingId, userId, resourceId,
            resourceType.name(), rating.getStars(), review.status.name()));

        if (review.status == ReviewStatus.APPROVED) {
            review.registerEvent(new ResourceRatingUpdatedEvent(
                id.getValue(), resourceId, resourceType.name()));
        }

        return review;
    }

    public static Review reconstitute(ReviewId id, String bookingId, String userId,
                                      String resourceId, ReviewedResourceType resourceType,
                                      Rating rating, ReviewContent content, ReviewStatus status,
                                      String moderatorId, String moderationReason,
                                      Instant createdAt, Instant updatedAt) {
        Review r = new Review(id, bookingId, userId, resourceId, resourceType, rating, content);
        r.status            = status;
        r.moderatorId        = moderatorId;
        r.moderationReason   = moderationReason;
        return r;
    }

    // ── Moderation ────────────────────────────────────────────────────────────

    public void approve(String moderatorId) {
        assertModerable();
        this.status           = ReviewStatus.APPROVED;
        this.moderatorId       = moderatorId;
        this.moderationReason  = null;
        this.updatedAt         = Instant.now();
        registerEvent(new ReviewModeratedEvent(
            getId().getValue(), bookingId, userId, "APPROVED", moderatorId));
        registerEvent(new ResourceRatingUpdatedEvent(
            getId().getValue(), resourceId, resourceType.name()));
    }

    public void reject(String moderatorId, String reason) {
        assertModerable();
        if (reason == null || reason.isBlank())
            throw new BusinessRuleViolationException(
                "A reason is required when rejecting a review", "MODERATION_REASON_REQUIRED");
        this.status           = ReviewStatus.REJECTED;
        this.moderatorId        = moderatorId;
        this.moderationReason   = reason;
        this.updatedAt          = Instant.now();
        registerEvent(new ReviewModeratedEvent(
            getId().getValue(), bookingId, userId, "REJECTED", moderatorId));
        // Fires even if this review was never APPROVED — a no-op
        // recompute if it wasn't previously counted, correct removal
        // if it was (see class Javadoc).
        registerEvent(new ResourceRatingUpdatedEvent(
            getId().getValue(), resourceId, resourceType.name()));
    }

    public void flag(String reporterId, String reason) {
        if (status != ReviewStatus.APPROVED)
            throw new BusinessRuleViolationException(
                "Only an approved review can be flagged for re-review",
                "INVALID_STATUS_TRANSITION");
        if (reporterId.equals(userId))
            throw new BusinessRuleViolationException(
                "Cannot flag your own review", "CANNOT_FLAG_OWN_REVIEW");
        this.status    = ReviewStatus.FLAGGED;
        this.updatedAt = Instant.now();
        registerEvent(new ReviewFlaggedEvent(
            getId().getValue(), resourceId, resourceType.name(), reporterId, reason));
    }

    private void assertModerable() {
        if (status != ReviewStatus.PENDING_MODERATION && status != ReviewStatus.FLAGGED)
            throw new BusinessRuleViolationException(
                "Cannot moderate a review in status: " + status, "INVALID_STATUS_TRANSITION");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String               getBookingId()        { return bookingId; }
    public String               getUserId()            { return userId; }
    public String               getResourceId()        { return resourceId; }
    public ReviewedResourceType getResourceType()      { return resourceType; }
    public Rating                getRating()            { return rating; }
    public ReviewContent         getContent()           { return content; }
    public ReviewStatus          getStatus()            { return status; }
    public String                getModeratorId()       { return moderatorId; }
    public String                getModerationReason()  { return moderationReason; }
    public Instant                getCreatedAt()         { return createdAt; }
    public Instant                getUpdatedAt()         { return updatedAt; }
}
