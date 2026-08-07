package com.travel.review.application.dto.response;

import com.travel.review.domain.aggregate.Review;
import java.time.Instant;

public record ReviewResponse(
    String  reviewId,
    String  bookingId,
    String  resourceId,
    String  resourceType,
    int     rating,
    String  title,
    String  body,
    String  status,
    String  moderatorId,
    String  moderationReason,
    Instant createdAt
) {
    public static ReviewResponse from(Review r) {
        return new ReviewResponse(
            r.getId().getValue(), r.getBookingId(), r.getResourceId(),
            r.getResourceType().name(), r.getRating().getStars(),
            r.getContent().getTitle(), r.getContent().getBody(),
            r.getStatus().name(), r.getModeratorId(), r.getModerationReason(),
            r.getCreatedAt());
    }
}
