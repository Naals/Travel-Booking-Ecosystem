package com.travel.review.application.usecase;

import com.travel.common.response.PagedResponse;
import com.travel.review.application.dto.response.ReviewResponse;
import com.travel.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetModerationQueueUseCase {

    private final ReviewRepository repository;

    public PagedResponse<ReviewResponse> execute(int page, int size) {
        var reviews = repository.findPendingModeration(page, size)
            .stream().map(ReviewResponse::from).toList();
        long total = repository.countPendingModeration();
        return PagedResponse.of(reviews, page, size, total);
    }
}
