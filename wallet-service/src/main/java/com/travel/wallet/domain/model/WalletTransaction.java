package com.travel.wallet.domain.model;

import com.travel.wallet.domain.valueobject.Money;
import com.travel.wallet.domain.valueobject.WalletTransactionId;
import com.travel.shared.domain.Entity;

import java.time.Instant;

/**
 * A single ledger entry, owned by the Wallet aggregate — mirrors the
 * "owned entity with its own identity but no independent lifecycle"
 * pattern used by Room (Hotel, Day 11), Seat (Flight, Day 12), and
 * SavedLocation (UserProfile, Day 15).
 *
 * balanceAfter is denormalized deliberately — the same running-balance
 * pattern a bank statement uses, so transaction history never needs to
 * replay the ledger from the start to show "balance after this entry."
 */
public class WalletTransaction extends Entity<WalletTransactionId> {

    private final WalletTransactionType type;
    private final Money   amount;
    private final Money   balanceAfter;
    private final String  referenceId;   // idempotency key, nullable
    private final String  description;
    private final Instant occurredAt;

    public WalletTransaction(WalletTransactionId id, WalletTransactionType type,
                             Money amount, Money balanceAfter, String referenceId,
                             String description, Instant occurredAt) {
        super(id);
        this.type         = type;
        this.amount        = amount;
        this.balanceAfter   = balanceAfter;
        this.referenceId     = referenceId;
        this.description      = description;
        this.occurredAt         = occurredAt;
    }

    public WalletTransactionType getType()         { return type; }
    public Money                  getAmount()       { return amount; }
    public Money                  getBalanceAfter() { return balanceAfter; }
    public String                 getReferenceId()  { return referenceId; }
    public String                 getDescription()  { return description; }
    public Instant                 getOccurredAt()   { return occurredAt; }
}
