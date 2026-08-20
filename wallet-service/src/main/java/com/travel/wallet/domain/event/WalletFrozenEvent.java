package com.travel.wallet.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * No consumer exists yet — published for a future fraud-service
 * (Tier 4, not yet built) to potentially react to, following the same
 * "publish now, document the intended future consumer" precedent as
 * SavedLocationAddedEvent (user-service, Day 15).
 */
public class WalletFrozenEvent extends DomainEvent {

    private final String userId;
    private final String reason;

    public WalletFrozenEvent(String userId, String reason) {
        super("WalletFrozen");
        this.userId = userId;
        this.reason = reason;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId() { return userId; }
    public String getReason() { return reason; }
}
