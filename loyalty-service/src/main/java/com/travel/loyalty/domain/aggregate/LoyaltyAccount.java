package com.travel.loyalty.domain.aggregate;

import com.travel.loyalty.domain.event.*;
import com.travel.loyalty.domain.model.*;
import com.travel.loyalty.domain.valueobject.*;
import com.travel.loyalty.domain.service.TierCalculationPolicy;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;
import java.util.*;

/**
 * LoyaltyAccount Aggregate Root.
 *
 * Tracks two separate point figures, which is the central domain
 * decision this service makes — see ADR-011 for the full reasoning,
 * summarized here: `balance` is spendable and decreases on redemption;
 * `lifetimePointsEarned` never decreases and is the sole input to tier
 * calculation. Redeeming points can never demote your tier, matching
 * how real airline/hotel loyalty programs behave.
 *
 * earnPoints()/redeemPoints() mirror Wallet.credit()/debit()
 * (wallet-service, Day 18) exactly in shape — a typed transaction
 * parameter, symmetric guard assertions, an appended ledger entry in
 * the same call that mutates balance. Deliberately reused, not
 * re-derived: same problem, same shape.
 *
 * Follows the createdAt-preserving reconstitute() pattern established
 * by Conversation/Message (Day 17) and Wallet (Day 18) — the private
 * constructor takes createdAt as a parameter rather than unconditionally
 * calling Instant.now(). See SERVICE_STATUS.md's Known Issues for which
 * earlier aggregates still have the bug this avoids.
 */
public class LoyaltyAccount extends AggregateRoot<LoyaltyAccountId> {

    private Points                        balance;
    private Points                        lifetimePointsEarned;
    private LoyaltyTier                   tier;
    private final List<LoyaltyTransaction> transactions;
    private final Instant                 createdAt;
    private Instant                       updatedAt;

    private LoyaltyAccount(LoyaltyAccountId id, Instant createdAt) {
        super(id);
        this.balance              = Points.zero();
        this.lifetimePointsEarned = Points.zero();
        this.tier                 = LoyaltyTier.BRONZE;
        this.transactions         = new ArrayList<>();
        this.createdAt            = createdAt;
        this.updatedAt            = createdAt;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static LoyaltyAccount provision(LoyaltyAccountId userId) {
        LoyaltyAccount account = new LoyaltyAccount(userId, Instant.now());
        account.registerEvent(new LoyaltyAccountCreatedEvent(userId.getValue()));
        return account;
    }

    public static LoyaltyAccount reconstitute(LoyaltyAccountId id, Points balance,
                                              Points lifetimePointsEarned, LoyaltyTier tier,
                                              List<LoyaltyTransaction> transactions,
                                              Instant createdAt, Instant updatedAt) {
        LoyaltyAccount account = new LoyaltyAccount(id, createdAt);
        account.balance              = balance;
        account.lifetimePointsEarned = lifetimePointsEarned;
        account.tier                 = tier;
        account.transactions.addAll(transactions != null ? transactions : List.of());
        account.updatedAt            = updatedAt;
        return account;
    }

    // ── Mutations ──────────────────────────────────────────────────────────────

    /**
     * Credits points. Raises LoyaltyPointsEarnedEvent, and — if the
     * resulting lifetime total crosses into a new tier — also raises
     * LoyaltyTierChangedEvent from the same call, the same
     * one-mutation-two-events shape Property.placeReservation()
     * (Day 10) and auto-approved Review.write() (Day 16) both use.
     */
    public LoyaltyTransaction earnPoints(Points points, LoyaltyTransactionType type,
                                         String referenceId, String description) {
        assertCredit(type);

        Points newBalance = this.balance.add(points);
        Points newLifetime = this.lifetimePointsEarned.add(points);

        LoyaltyTransaction tx = new LoyaltyTransaction(
            LoyaltyTransactionId.generate(), type, points, newBalance,
            referenceId, description, Instant.now());

        this.balance              = newBalance;
        this.lifetimePointsEarned = newLifetime;
        transactions.add(tx);
        this.updatedAt = Instant.now();

        registerEvent(new LoyaltyPointsEarnedEvent(
            getId().getValue(), tx.getId().getValue(),
            points.getValue(), newBalance.getValue(), description));

        LoyaltyTier newTier = TierCalculationPolicy.calculateTier(newLifetime);
        if (newTier != this.tier) {
            LoyaltyTier previousTier = this.tier;
            this.tier = newTier;
            registerEvent(new LoyaltyTierChangedEvent(
                getId().getValue(), previousTier.name(), newTier.name()));
        }

        return tx;
    }

    /**
     * Debits points. lifetimePointsEarned is untouched — see class
     * Javadoc and ADR-011.
     */
    public LoyaltyTransaction redeemPoints(Points points, LoyaltyTransactionType type,
                                           String referenceId, String description) {
        assertDebit(type);

        if (this.balance.isLessThan(points))
            throw new BusinessRuleViolationException(
                "Insufficient points balance", "INSUFFICIENT_POINTS");

        Points newBalance = this.balance.subtract(points);
        LoyaltyTransaction tx = new LoyaltyTransaction(
            LoyaltyTransactionId.generate(), type, points, newBalance,
            referenceId, description, Instant.now());

        this.balance = newBalance;
        transactions.add(tx);
        this.updatedAt = Instant.now();

        registerEvent(new LoyaltyPointsRedeemedEvent(
            getId().getValue(), tx.getId().getValue(), type.name(),
            points.getValue(), newBalance.getValue(), description));

        return tx;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void assertCredit(LoyaltyTransactionType type) {
        if (!type.isCredit())
            throw new BusinessRuleViolationException(
                type.name() + " is not a credit transaction type", "INVALID_TRANSACTION_TYPE");
    }

    private void assertDebit(LoyaltyTransactionType type) {
        if (type.isCredit())
            throw new BusinessRuleViolationException(
                type.name() + " is not a debit transaction type", "INVALID_TRANSACTION_TYPE");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Points                   getBalance()              { return balance; }
    public Points                   getLifetimePointsEarned() { return lifetimePointsEarned; }
    public LoyaltyTier               getTier()                  { return tier; }
    public List<LoyaltyTransaction>  getTransactions()          { return Collections.unmodifiableList(transactions); }
    public Instant                    getCreatedAt()             { return createdAt; }
    public Instant                    getUpdatedAt()             { return updatedAt; }
}
