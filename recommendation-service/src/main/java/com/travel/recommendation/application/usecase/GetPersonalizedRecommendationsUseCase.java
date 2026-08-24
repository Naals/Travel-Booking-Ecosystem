package com.travel.recommendation.application.usecase;

import com.travel.recommendation.application.dto.response.DestinationRecommendationResponse;
import com.travel.recommendation.domain.repository.DestinationPopularityRepository;
import com.travel.recommendation.domain.repository.UserAffinityRepository;
import com.travel.recommendation.domain.service.RecommendationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPersonalizedRecommendationsUseCase {

    /** Candidate pool size — bounds the popularity side of the merge before ranking. */
    private static final int POPULARITY_CANDIDATE_POOL = 50;

    private final UserAffinityRepository          affinityRepository;
    private final DestinationPopularityRepository popularityRepository;

    @Transactional(readOnly = true)
    public List<DestinationRecommendationResponse> execute(String userId, int limit) {
        var affinities  = affinityRepository.findByUserId(userId);
        var popularities = popularityRepository.findTopByCompletedTripCount(POPULARITY_CANDIDATE_POOL);

        return RecommendationEngine.rank(affinities, popularities, limit).stream()
            .map(DestinationRecommendationResponse::from)
            .toList();
    }
}
