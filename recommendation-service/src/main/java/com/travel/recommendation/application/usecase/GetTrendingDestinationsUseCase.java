package com.travel.recommendation.application.usecase;

import com.travel.recommendation.application.dto.response.TrendingDestinationResponse;
import com.travel.recommendation.domain.repository.DestinationPopularityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTrendingDestinationsUseCase {

    private final DestinationPopularityRepository repository;

    @Transactional(readOnly = true)
    public List<TrendingDestinationResponse> execute(int limit) {
        return repository.findTopByCompletedTripCount(limit).stream()
            .map(TrendingDestinationResponse::from)
            .toList();
    }
}
