package com.travel.review.domain.repository;

import com.travel.review.domain.model.ReviewEligibility;
import java.util.Optional;

public interface ReviewEligibilityRepository {

    void recordEligibility(ReviewEligibility eligibility);

    /**
     * Atomically finds an unconsumed eligibility record for
     * (bookingId, userId) and marks it consumed in the same operation.
     * Two concurrent calls for the same booking cannot both succeed —
     * see ReviewEligibilityRepositoryAdapter for the MongoDB mechanism.
     */
    Optional<ReviewEligibility> tryConsume(String bookingId, String userId);
}
