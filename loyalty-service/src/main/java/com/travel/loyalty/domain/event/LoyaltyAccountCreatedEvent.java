package com.travel.loyalty.domain.event;

import com.travel.shared.event.DomainEvent;

public class LoyaltyAccountCreatedEvent extends DomainEvent {

    private final String userId;

    public LoyaltyAccountCreatedEvent(String userId) {
        super("LoyaltyAccountCreated");
        this.userId = userId;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId() { return userId; }
}
