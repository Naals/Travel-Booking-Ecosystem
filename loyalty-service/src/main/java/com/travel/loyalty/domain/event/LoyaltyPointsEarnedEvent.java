package com.travel.loyalty.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * First real producer of KafkaTopics.LOYALTY_POINTS_EARNED — declared
 * in common-lib since Day 3, ahead of any service that would actually
 * publish to it. Fourth instance of this exact pattern, after
 * REVIEW_CREATED (Day 3 → 16), MESSAGE_SENT (Day 3 → 17), and
 * WALLET_CREDITED (Day 3 → 18).
 *
 * Also the trigger for closing a real gap: NotificationType.
 * LOYALTY_POINTS_EARNED and its switch-statement cases have existed in
 * notification-service since Day 9 with no producer to ever fire them
 * and no template file backing them — see this day's commit 9.
 */
public class LoyaltyPointsEarnedEvent extends DomainEvent {

    private final String userId;
    private final String transactionId;
    private final long   pointsEarned;
    private final long   newBalance;
    private final String description;

    public LoyaltyPointsEarnedEvent(String userId, String transactionId, long pointsEarned,
                                    long newBalance, String description) {
        super("LoyaltyPointsEarned");
        this.userId        = userId;
        this.transactionId = transactionId;
        this.pointsEarned  = pointsEarned;
        this.newBalance    = newBalance;
        this.description   = description;
    }

    @Override public String getAggregateId()  { return userId; }
    public String getUserId()          { return userId; }
    public String getTransactionId()   { return transactionId; }
    public long   getPointsEarned()    { return pointsEarned; }
    public long   getNewBalance()      { return newBalance; }
    public String getDescription()     { return description; }
}
