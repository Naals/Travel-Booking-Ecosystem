package com.travel.review.application.dto.response;

import com.travel.review.domain.model.RatingSummary;

public record RatingSummaryResponse(
    String resourceId,
    double averageRating,
    long   reviewCount
) {
    public static RatingSummaryResponse from(RatingSummary s) {
        return new RatingSummaryResponse(s.getResourceId(), s.getAverageRating(), s.getReviewCount());
    }

    public static RatingSummaryResponse empty(String resourceId) {
        return new RatingSummaryResponse(resourceId, 0.0, 0L);
    }
}
