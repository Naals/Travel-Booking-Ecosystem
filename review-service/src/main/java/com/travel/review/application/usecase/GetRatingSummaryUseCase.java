package com.travel.review.application.usecase;

import com.travel.review.application.dto.response.RatingSummaryResponse;
import com.travel.review.domain.repository.RatingSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetRatingSummaryUseCase {

    private final RatingSummaryRepository repository;

    public RatingSummaryResponse execute(String resourceId) {
        return repository.findByResourceId(resourceId)
            .map(RatingSummaryResponse::from)
            .orElse(RatingSummaryResponse.empty(resourceId));
    }
}
