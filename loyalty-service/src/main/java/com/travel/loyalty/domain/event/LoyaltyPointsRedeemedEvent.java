package com.travel.loyalty.domain.event;

import com.travel.shared.event.DomainEvent;

public class LoyaltyPointsRedeemedEvent extends DomainEvent {

    private final String userId;
    private final String transactionId;
    private final String transactionType; // REDEEMED or ADMIN_DEBIT
    private final long   pointsSpent;
    private final long   newBalance;
    private final String description;

    public LoyaltyPointsRedeemedEvent(String userId, String transactionId, String transactionType,
                                      long pointsSpent, long newBalance, String description) {
        super("LoyaltyPointsRedeemed");
        this.userId          = userId;
        this.transactionId   = transactionId;
        this.transactionType = transactionType;
        this.pointsSpent     = pointsSpent;
        this.newBalance      = newBalance;
        this.description     = description;
    }

    @Override public String getAggregateId()  { return userId; }
    public String getUserId()          { return userId; }
    public String getTransactionId()   { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public long   getPointsSpent()     { return pointsSpent; }
    public long   getNewBalance()      { return newBalance; }
    public String getDescription()     { return description; }
}
