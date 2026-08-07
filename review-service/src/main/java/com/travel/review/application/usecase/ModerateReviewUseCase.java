package com.travel.review.application.usecase;

import com.travel.review.application.dto.response.ReviewResponse;
import com.travel.review.domain.aggregate.Review;
import com.travel.review.domain.repository.ReviewRepository;
import com.travel.review.domain.valueobject.ReviewId;
import com.travel.review.infrastructure.messaging.producer.ReviewEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModerateReviewUseCase {

    private final ReviewRepository     reviewRepository;
    private final ReviewEventPublisher eventPublisher;

    public ReviewResponse approve(String reviewId, String moderatorId) {
        Review review = load(reviewId);
        review.approve(moderatorId);
        return persist(review);
    }

    public ReviewResponse reject(String reviewId, String moderatorId, String reason) {
        Review review = load(reviewId);
        review.reject(moderatorId, reason);
        return persist(review);
    }

    private Review load(String reviewId) {
        return reviewRepository.findById(ReviewId.of(reviewId))
            .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
    }

    private ReviewResponse persist(Review review) {
        Review saved = reviewRepository.save(review);
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();
        return ReviewResponse.from(saved);
    }
}
