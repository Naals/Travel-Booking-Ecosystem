package com.travel.review.domain.repository;

import com.travel.review.domain.model.RatingSummary;
import java.util.Optional;

public interface RatingSummaryRepository {
    void save(RatingSummary summary);
    Optional<RatingSummary> findByResourceId(String resourceId);
}
