package com.travel.loyalty.domain.model;

/**
 * Membership tier, ordered lowest to highest. Declaration order is
 * meaningful — see TierCalculationPolicy, which walks this array from
 * highest to lowest looking for the first threshold the account's
 * lifetime points clear.
 */
public enum LoyaltyTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
}
