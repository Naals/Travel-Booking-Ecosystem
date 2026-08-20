package com.travel.wallet.domain.event;

import com.travel.shared.event.DomainEvent;
import com.travel.wallet.domain.valueobject.Money;

public class WalletDebitedEvent extends DomainEvent {

    private final String userId;
    private final String transactionId;
    private final String transactionType;
    private final Money  amount;
    private final Money  newBalance;
    private final String description;

    public WalletDebitedEvent(String userId, String transactionId, String transactionType,
                              Money amount, Money newBalance, String description) {
        super("WalletDebited");
        this.userId          = userId;
        this.transactionId   = transactionId;
        this.transactionType = transactionType;
        this.amount          = amount;
        this.newBalance      = newBalance;
        this.description     = description;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId()          { return userId; }
    public String getTransactionId()   { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public Money  getAmount()          { return amount; }
    public Money  getNewBalance()      { return newBalance; }
    public String getDescription()     { return description; }
}
