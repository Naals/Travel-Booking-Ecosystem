package com.travel.review.application.usecase;

import com.travel.common.response.PagedResponse;
import com.travel.review.application.dto.response.ReviewResponse;
import com.travel.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetReviewsForResourceUseCase {

    private final ReviewRepository repository;

    public PagedResponse<ReviewResponse> execute(String resourceId, int page, int size) {
        var reviews = repository.findApprovedByResourceId(resourceId, page, size)
            .stream().map(ReviewResponse::from).toList();
        long total = repository.countApprovedByResourceId(resourceId);
        return PagedResponse.of(reviews, page, size, total);
    }
}
