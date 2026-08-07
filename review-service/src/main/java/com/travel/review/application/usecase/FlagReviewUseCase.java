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
public class FlagReviewUseCase {

    private final ReviewRepository     reviewRepository;
    private final ReviewEventPublisher eventPublisher;

    public ReviewResponse execute(String reviewId, String reporterId, String reason) {
        Review review = reviewRepository.findById(ReviewId.of(reviewId))
            .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        review.flag(reporterId, reason);
        Review saved = reviewRepository.save(review);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        return ReviewResponse.from(saved);
    }
}
