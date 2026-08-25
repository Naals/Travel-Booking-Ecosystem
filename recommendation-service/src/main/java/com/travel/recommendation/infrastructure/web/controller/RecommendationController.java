package com.travel.recommendation.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.recommendation.application.dto.response.DestinationRecommendationResponse;
import com.travel.recommendation.application.dto.response.TrendingDestinationResponse;
import com.travel.recommendation.application.usecase.GetPersonalizedRecommendationsUseCase;
import com.travel.recommendation.application.usecase.GetTrendingDestinationsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Personalized destination recommendations and trending destinations")
public class RecommendationController {

    private final GetPersonalizedRecommendationsUseCase personalizedUseCase;
    private final GetTrendingDestinationsUseCase        trendingUseCase;

    @GetMapping("/me")
    @Operation(summary = "Get personalized destination recommendations for the authenticated user")
    public ResponseEntity<ApiResponse<List<DestinationRecommendationResponse>>> getMyRecommendations(
        @RequestHeader("X-User-Id") String userId,
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(personalizedUseCase.execute(userId, limit)));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get globally trending destinations")
    public ResponseEntity<ApiResponse<List<TrendingDestinationResponse>>> getTrending(
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(trendingUseCase.execute(limit)));
    }
}
