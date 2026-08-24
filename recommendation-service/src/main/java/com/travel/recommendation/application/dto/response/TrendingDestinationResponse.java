package com.travel.recommendation.application.dto.response;

import com.travel.recommendation.domain.model.DestinationPopularity;

public record TrendingDestinationResponse(
    String city,
    String country,
    long   completedTripCount
) {
    public static TrendingDestinationResponse from(DestinationPopularity p) {
        return new TrendingDestinationResponse(
            p.getDestination().getCity(), p.getDestination().getCountry(),
            p.getCompletedTripCount());
    }
}
