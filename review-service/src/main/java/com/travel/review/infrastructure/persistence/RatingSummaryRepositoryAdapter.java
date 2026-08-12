package com.travel.review.infrastructure.persistence;

import com.travel.review.domain.model.RatingSummary;
import com.travel.review.domain.model.ReviewedResourceType;
import com.travel.review.domain.repository.RatingSummaryRepository;
import com.travel.review.infrastructure.persistence.document.RatingSummaryDocument;
import com.travel.review.infrastructure.persistence.repository.RatingSummaryMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RatingSummaryRepositoryAdapter implements RatingSummaryRepository {

    private final RatingSummaryMongoRepository mongo;

    @Override
    public void save(RatingSummary summary) {
        mongo.save(RatingSummaryDocument.builder()
            .resourceId(summary.getResourceId())
            .resourceType(summary.getResourceType().name())
            .averageRating(summary.getAverageRating())
            .reviewCount(summary.getReviewCount())
            .updatedAt(summary.getUpdatedAt())
            .build());
    }

    @Override
    public Optional<RatingSummary> findByResourceId(String resourceId) {
        return mongo.findById(resourceId).map(d -> RatingSummary.of(
            d.getResourceId(), ReviewedResourceType.valueOf(d.getResourceType()),
            d.getAverageRating(), d.getReviewCount()));
    }
}
