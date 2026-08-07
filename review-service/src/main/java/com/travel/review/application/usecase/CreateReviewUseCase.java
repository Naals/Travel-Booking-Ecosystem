package com.travel.review.application.usecase;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.review.application.dto.request.CreateReviewRequest;
import com.travel.review.application.dto.response.ReviewResponse;
import com.travel.review.domain.aggregate.Review;
import com.travel.review.domain.model.ReviewEligibility;
import com.travel.review.domain.repository.ReviewEligibilityRepository;
import com.travel.review.domain.repository.ReviewRepository;
import com.travel.review.domain.service.ContentModerationPolicy;
import com.travel.review.domain.valueobject.Rating;
import com.travel.review.domain.valueobject.ReviewContent;
import com.travel.review.infrastructure.messaging.producer.ReviewEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateReviewUseCase {

    private final ReviewEligibilityRepository eligibilityRepository;
    private final ReviewRepository            reviewRepository;
    private final ContentModerationPolicy     moderationPolicy;
    private final ReviewEventPublisher        eventPublisher;

    public ReviewResponse execute(String userId, CreateReviewRequest request) {
        // Atomic consume — two concurrent submissions for the same
        // booking cannot both succeed. See tryConsume()'s Javadoc.
        ReviewEligibility eligibility = eligibilityRepository
            .tryConsume(request.bookingId(), userId)
            .orElseThrow(() -> new BusinessRuleViolationException(
                "This booking is not eligible for review — it may not be completed, " +
                    "may not belong to you, or may already have been reviewed",
                "NOT_ELIGIBLE_FOR_REVIEW"));

        Rating        rating  = Rating.of(request.rating());
        ReviewContent content = ReviewContent.of(request.title(), request.body());
        boolean       needsManualReview = moderationPolicy.requiresManualReview(content);

        Review review = Review.write(
            eligibility.getBookingId(), userId,
            eligibility.getResourceId(), eligibility.getResourceType(),
            rating, content, needsManualReview);

        Review saved = reviewRepository.save(review);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Review created: {} status={}", saved.getId().getValue(), saved.getStatus());
        return ReviewResponse.from(saved);
    }
}
