package com.travel.wallet.domain.event;

import com.travel.shared.event.DomainEvent;
import com.travel.wallet.domain.valueobject.Money;

/**
 * First real producer of KafkaTopics.WALLET_CREDITED — declared in
 * common-lib back on Day 3, ahead of any service that would actually
 * publish to it. Same pattern as REVIEW_CREATED (Day 3 → 16) and
 * MESSAGE_SENT (Day 3 → 17); this is the third instance of it.
 */
public class WalletCreditedEvent extends DomainEvent {

    private final String userId;
    private final String transactionId;
    private final String transactionType;
    private final Money  amount;
    private final Money  newBalance;
    private final String description;

    public WalletCreditedEvent(String userId, String transactionId, String transactionType,
                               Money amount, Money newBalance, String description) {
        super("WalletCredited");
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
