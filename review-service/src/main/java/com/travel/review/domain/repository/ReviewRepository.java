package com.travel.review.domain.repository;

import com.travel.review.domain.aggregate.Review;
import com.travel.review.domain.model.ReviewStatus;
import com.travel.review.domain.valueobject.ReviewId;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review           save(Review review);
    Optional<Review> findById(ReviewId id);
    List<Review>     findByResourceIdAndStatus(String resourceId, ReviewStatus status);
    List<Review>     findApprovedByResourceId(String resourceId, int page, int size);
    long             countApprovedByResourceId(String resourceId);
    List<Review>     findPendingModeration(int page, int size);
    long             countPendingModeration();
}
