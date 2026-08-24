package com.travel.recommendation.application.dto.response;

import com.travel.recommendation.domain.model.DestinationScore;

public record DestinationRecommendationResponse(
    String city,
    String country,
    long   score
) {
    public static DestinationRecommendationResponse from(DestinationScore s) {
        return new DestinationRecommendationResponse(
            s.destination().getCity(), s.destination().getCountry(), s.score());
    }
}
