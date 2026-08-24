package com.travel.recommendation.domain.service;

import com.travel.recommendation.domain.model.DestinationPopularity;
import com.travel.recommendation.domain.model.DestinationScore;
import com.travel.recommendation.domain.model.UserAffinity;
import com.travel.recommendation.domain.valueobject.DestinationKey;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure ranking function — no Spring dependency, no state, same shape
 * as TierCalculationPolicy (loyalty-service, Day 19). Combines a
 * user's personal affinity scores with global trending counts into a
 * single ranked list.
 *
 * A brand-new user with no affinity signals naturally receives a
 * pure-popularity ("trending") ranking — intentional graceful
 * degradation, not a special case that needed handling separately.
 */
public final class RecommendationEngine {

    /** Multiplier applied to raw affinity score when merging with popularity. */
    private static final int AFFINITY_WEIGHT = 2;

    private RecommendationEngine() {}

    public static List<DestinationScore> rank(List<UserAffinity> affinities,
                                              List<DestinationPopularity> popularities,
                                              int limit) {
        Map<DestinationKey, Long> combined = new HashMap<>();

        for (DestinationPopularity p : popularities) {
            combined.merge(p.getDestination(), p.getCompletedTripCount(), Long::sum);
        }
        for (UserAffinity a : affinities) {
            combined.merge(a.getDestination(), a.getScore() * AFFINITY_WEIGHT, Long::sum);
        }

        return combined.entrySet().stream()
            .map(e -> new DestinationScore(e.getKey(), e.getValue()))
            .sorted(Comparator.comparingLong(DestinationScore::score).reversed())
            .limit(limit)
            .toList();
    }
}
