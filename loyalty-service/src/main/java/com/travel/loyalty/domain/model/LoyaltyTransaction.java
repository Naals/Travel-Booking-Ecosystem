package com.travel.loyalty.domain.model;

import com.travel.loyalty.domain.valueobject.LoyaltyTransactionId;
import com.travel.loyalty.domain.valueobject.Points;
import com.travel.shared.domain.Entity;

import java.time.Instant;

/**
 * A single ledger entry, owned by LoyaltyAccount. Structural mirror of
 * WalletTransaction (wallet-service, Day 18), balanceAfter denormalized
 * for the same reason — see that class's Javadoc.
 */
public class LoyaltyTransaction extends Entity<LoyaltyTransactionId> {

    private final LoyaltyTransactionType type;
    private final Points  points;
    private final Points  balanceAfter;
    private final String  referenceId; // bookingId for EARNED, nullable otherwise
    private final String  description;
    private final Instant occurredAt;

    public LoyaltyTransaction(LoyaltyTransactionId id, LoyaltyTransactionType type,
                              Points points, Points balanceAfter, String referenceId,
                              String description, Instant occurredAt) {
        super(id);
        this.type         = type;
        this.points        = points;
        this.balanceAfter   = balanceAfter;
        this.referenceId     = referenceId;
        this.description      = description;
        this.occurredAt         = occurredAt;
    }

    public LoyaltyTransactionType getType()         { return type; }
    public Points                  getPoints()       { return points; }
    public Points                  getBalanceAfter() { return balanceAfter; }
    public String                  getReferenceId()  { return referenceId; }
    public String                  getDescription()  { return description; }
    public Instant                  getOccurredAt()   { return occurredAt; }
}
