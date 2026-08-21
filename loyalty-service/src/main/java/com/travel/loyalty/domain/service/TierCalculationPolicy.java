package com.travel.loyalty.domain.service;

import com.travel.loyalty.domain.model.LoyaltyTier;
import com.travel.loyalty.domain.valueobject.Points;

/**
 * Pure function from lifetime points earned to membership tier — no
 * Spring dependency, no state, same "small deterministic domain
 * service" shape as ContentModerationPolicy (review-service, Day 16).
 * Thresholds are illustrative, not tuned against any real economics.
 */
public final class TierCalculationPolicy {

    private static final long SILVER_THRESHOLD   = 5_000L;
    private static final long GOLD_THRESHOLD      = 20_000L;
    private static final long PLATINUM_THRESHOLD  = 50_000L;

    private TierCalculationPolicy() {}

    public static LoyaltyTier calculateTier(Points lifetimePointsEarned) {
        long lifetime = lifetimePointsEarned.getValue();
        if (lifetime >= PLATINUM_THRESHOLD) return LoyaltyTier.PLATINUM;
        if (lifetime >= GOLD_THRESHOLD)      return LoyaltyTier.GOLD;
        if (lifetime >= SILVER_THRESHOLD)    return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }
}
