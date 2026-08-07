package com.travel.review.domain.service;

import com.travel.review.domain.aggregate.Review;
import com.travel.review.domain.model.RatingSummary;
import com.travel.review.domain.model.ReviewStatus;
import com.travel.review.domain.model.ReviewedResourceType;
import com.travel.review.domain.repository.RatingSummaryRepository;
import com.travel.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Recomputes a resource's rating summary from its current set of
 * APPROVED reviews. Called by RatingRecomputeConsumer, never directly
 * from a use case — see that consumer's Javadoc for why the recompute
 * is decoupled from the write that triggers it.
 */
@Service
@RequiredArgsConstructor
public class RatingAggregationService {

    private final ReviewRepository        reviewRepository;
    private final RatingSummaryRepository ratingSummaryRepository;

    public void recompute(String resourceId, ReviewedResourceType resourceType) {
        List<Review> approved =
            reviewRepository.findByResourceIdAndStatus(resourceId, ReviewStatus.APPROVED);

        long   count   = approved.size();
        double average = count == 0 ? 0.0 : approved.stream()
            .mapToInt(r -> r.getRating().getStars())
            .average()
            .orElse(0.0);

        ratingSummaryRepository.save(RatingSummary.of(resourceId, resourceType, average, count));
    }
}
