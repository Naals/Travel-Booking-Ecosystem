package com.travel.recommendation.domain.model;

import com.travel.recommendation.domain.valueobject.DestinationKey;

/** Output of RecommendationEngine.rank() — a ranked (destination, combined score) pair. */
public record DestinationScore(DestinationKey destination, long score) {}
