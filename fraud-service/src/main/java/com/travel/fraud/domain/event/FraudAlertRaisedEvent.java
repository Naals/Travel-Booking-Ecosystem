package com.travel.fraud.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * First real producer of KafkaTopics.FRAUD_ALERT_RAISED — declared in
 * common-lib since Day 3. Fifth instance of the "seeded ahead of its
 * producer" pattern, after REVIEW_CREATED (Day 3→16), MESSAGE_SENT
 * (Day 3→17), WALLET_CREDITED (Day 3→18), and LOYALTY_POINTS_EARNED
 * (Day 3→19).
 *
 * Consumed by a new wallet-service listener (this day) that
 * automatically freezes the user's wallet — see ADR-013.
 */
public class FraudAlertRaisedEvent extends DomainEvent {

    private final String userId;
    private final String ruleName;
    private final String reason;

    public FraudAlertRaisedEvent(String userId, String ruleName, String reason) {
        super("FraudAlertRaised");
        this.userId   = userId;
        this.ruleName = ruleName;
        this.reason   = reason;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId()   { return userId; }
    public String getRuleName() { return ruleName; }
    public String getReason()   { return reason; }
}
