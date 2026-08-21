package com.travel.loyalty.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Named "Changed" to match KafkaTopics.LOYALTY_TIER_CHANGED (Day 3),
 * but in this domain a tier "change" is always an upgrade — tier is
 * computed from lifetime points earned, which is monotonically
 * non-decreasing (see ADR-011), so a downgrade can never occur through
 * normal earnPoints() flow. notification-service's consumer (this
 * day's commit 9) maps this straight to NotificationType.
 * LOYALTY_TIER_UPGRADED on that basis.
 */
public class LoyaltyTierChangedEvent extends DomainEvent {

    private final String userId;
    private final String previousTier;
    private final String newTier;

    public LoyaltyTierChangedEvent(String userId, String previousTier, String newTier) {
        super("LoyaltyTierChanged");
        this.userId       = userId;
        this.previousTier = previousTier;
        this.newTier      = newTier;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId()       { return userId; }
    public String getPreviousTier() { return previousTier; }
    public String getNewTier()      { return newTier; }
}
