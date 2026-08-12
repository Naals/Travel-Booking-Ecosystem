package com.travel.review.infrastructure.persistence;

import com.travel.review.domain.model.ReviewEligibility;
import com.travel.review.domain.model.ReviewedResourceType;
import com.travel.review.domain.repository.ReviewEligibilityRepository;
import com.travel.review.infrastructure.persistence.document.ReviewEligibilityDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * tryConsume() uses MongoDB's atomic findAndModify rather than a
 * separate find-then-save, so two concurrent review submissions for
 * the same booking cannot both succeed — the second call's filter
 * (reviewed: false) simply won't match once the first has flipped
 * the flag, and findAndModify returns null on no match.
 */
@Component
@RequiredArgsConstructor
public class ReviewEligibilityRepositoryAdapter implements ReviewEligibilityRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public void recordEligibility(ReviewEligibility eligibility) {
        ReviewEligibilityDocument doc = ReviewEligibilityDocument.builder()
            .id(eligibility.getBookingId())
            .bookingId(eligibility.getBookingId())
            .userId(eligibility.getUserId())
            .resourceId(eligibility.getResourceId())
            .resourceType(eligibility.getResourceType().name())
            .reviewed(false)
            .completedAt(eligibility.getCompletedAt())
            .build();
        mongoTemplate.save(doc);
    }

    @Override
    public Optional<ReviewEligibility> tryConsume(String bookingId, String userId) {
        Query query = new Query(Criteria.where("_id").is(bookingId)
            .and("userId").is(userId)
            .and("reviewed").is(false));

        Update update = new Update().set("reviewed", true);

        ReviewEligibilityDocument doc = mongoTemplate.findAndModify(
            query, update,
            FindAndModifyOptions.options().returnNew(false),
            ReviewEligibilityDocument.class);

        if (doc == null) return Optional.empty();

        return Optional.of(ReviewEligibility.of(
            doc.getBookingId(), doc.getUserId(), doc.getResourceId(),
            ReviewedResourceType.valueOf(doc.getResourceType()), doc.getCompletedAt()));
    }
}
