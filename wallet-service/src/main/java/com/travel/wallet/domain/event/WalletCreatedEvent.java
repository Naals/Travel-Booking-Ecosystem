package com.travel.wallet.domain.event;

import com.travel.shared.event.DomainEvent;

public class WalletCreatedEvent extends DomainEvent {

    private final String userId;
    private final String currency;

    public WalletCreatedEvent(String userId, String currency) {
        super("WalletCreated");
        this.userId   = userId;
        this.currency = currency;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId()   { return userId; }
    public String getCurrency() { return currency; }
}
