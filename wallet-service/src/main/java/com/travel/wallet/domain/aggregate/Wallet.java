package com.travel.wallet.domain.aggregate;

import com.travel.wallet.domain.event.*;
import com.travel.wallet.domain.model.*;
import com.travel.wallet.domain.valueobject.*;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;
import java.util.*;

/**
 * Wallet Aggregate Root.
 *
 * One wallet per user, auto-provisioned reactively from
 * identity.user-registered — see CreateWalletUseCase and
 * IdentityEventConsumer. This is the second service (after
 * user-service, Day 15) to auto-provision itself from that same
 * upstream event.
 *
 * Balance is a cached/denormalized field, not derived by summing
 * transactions on every read — but every mutation goes through
 * credit()/debit(), which compute the new balance and append a
 * WalletTransaction in the same call, so the two can never drift as
 * long as all writes go through this aggregate, which hexagonal
 * architecture guarantees (the JPA layer never writes to wallets
 * directly).
 *
 * Unlike every prior aggregate's reconstitute() before Day 17 (see
 * SERVICE_STATUS.md "Known issues" — Booking, Property, Hotel,
 * Flight, Vehicle, UserProfile, and Review all discard the createdAt
 * they're given), this class follows the fix introduced in
 * Conversation/Message (Day 17): the private constructor takes
 * createdAt as a parameter instead of unconditionally calling
 * Instant.now().
 */
public class Wallet extends AggregateRoot<WalletId> {

    private static final String DEFAULT_CURRENCY = "USD"; // matches TravelPreferences.defaults() (Day 15)

    private Money                          balance;
    private final String                   currency;
    private WalletStatus                   status;
    private final List<WalletTransaction>  transactions;
    private final Instant                  createdAt;
    private Instant                        updatedAt;

    private Wallet(WalletId id, String currency, Instant createdAt) {
        super(id);
        this.balance      = Money.zero(currency);
        this.currency     = currency;
        this.status       = WalletStatus.ACTIVE;
        this.transactions = new ArrayList<>();
        this.createdAt    = createdAt;
        this.updatedAt    = createdAt;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static Wallet provision(WalletId userId) {
        Wallet wallet = new Wallet(userId, DEFAULT_CURRENCY, Instant.now());
        wallet.registerEvent(new WalletCreatedEvent(userId.getValue(), DEFAULT_CURRENCY));
        return wallet;
    }

    public static Wallet reconstitute(WalletId id, Money balance, String currency,
                                      WalletStatus status, List<WalletTransaction> transactions,
                                      Instant createdAt, Instant updatedAt) {
        Wallet wallet = new Wallet(id, currency, createdAt);
        wallet.balance = balance;
        wallet.status  = status;
        wallet.transactions.addAll(transactions != null ? transactions : List.of());
        wallet.updatedAt = updatedAt;
        return wallet;
    }

    // ── Mutations ──────────────────────────────────────────────────────────────

    /**
     * Credits the wallet. idempotencyKey (nullable) is checked against
     * previously recorded transaction references before appending —
     * see hasTransactionWithReference() — following the same
     * within-aggregate duplicate check Hotel.addRoom() (Day 11) and
     * Flight.addSeat() (Day 12) use for their own owned collections,
     * rather than a separate repository existence query.
     */
    public WalletTransaction credit(Money amount, WalletTransactionType type,
                                    String idempotencyKey, String description) {
        assertActive();
        assertCredit(type);
        assertNoDuplicateReference(idempotencyKey);

        Money newBalance = this.balance.add(amount);
        WalletTransaction tx = new WalletTransaction(
            WalletTransactionId.generate(), type, amount, newBalance,
            idempotencyKey, description, Instant.now());

        this.balance = newBalance;
        transactions.add(tx);
        this.updatedAt = Instant.now();

        registerEvent(new WalletCreditedEvent(
            getId().getValue(), tx.getId().getValue(), type.name(),
            amount, newBalance, description));

        return tx;
    }

    public WalletTransaction debit(Money amount, WalletTransactionType type,
                                   String idempotencyKey, String description) {
        assertActive();
        assertDebit(type);
        assertNoDuplicateReference(idempotencyKey);

        if (this.balance.isLessThan(amount))
            throw new BusinessRuleViolationException(
                "Insufficient wallet balance", "INSUFFICIENT_BALANCE");

        Money newBalance = this.balance.subtract(amount);
        WalletTransaction tx = new WalletTransaction(
            WalletTransactionId.generate(), type, amount, newBalance,
            idempotencyKey, description, Instant.now());

        this.balance = newBalance;
        transactions.add(tx);
        this.updatedAt = Instant.now();

        registerEvent(new WalletDebitedEvent(
            getId().getValue(), tx.getId().getValue(), type.name(),
            amount, newBalance, description));

        return tx;
    }

    public void freeze(String reason) {
        if (status == WalletStatus.FROZEN)
            throw new BusinessRuleViolationException("Wallet is already frozen", "ALREADY_FROZEN");
        this.status    = WalletStatus.FROZEN;
        this.updatedAt = Instant.now();
        registerEvent(new WalletFrozenEvent(getId().getValue(), reason));
    }

    public void unfreeze() {
        if (status != WalletStatus.FROZEN)
            throw new BusinessRuleViolationException("Wallet is not frozen", "NOT_FROZEN");
        this.status    = WalletStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void assertActive() {
        if (status == WalletStatus.FROZEN)
            throw new BusinessRuleViolationException("Cannot modify a frozen wallet", "WALLET_FROZEN");
    }

    private void assertCredit(WalletTransactionType type) {
        if (!type.isCredit())
            throw new BusinessRuleViolationException(
                type.name() + " is not a credit transaction type", "INVALID_TRANSACTION_TYPE");
    }

    private void assertDebit(WalletTransactionType type) {
        if (type.isCredit())
            throw new BusinessRuleViolationException(
                type.name() + " is not a debit transaction type", "INVALID_TRANSACTION_TYPE");
    }

    private void assertNoDuplicateReference(String idempotencyKey) {
        if (idempotencyKey != null && hasTransactionWithReference(idempotencyKey))
            throw new BusinessRuleViolationException(
                "A transaction with this reference has already been recorded",
                "DUPLICATE_TRANSACTION_REFERENCE");
    }

    private boolean hasTransactionWithReference(String reference) {
        return transactions.stream().anyMatch(t -> reference.equals(t.getReferenceId()));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Money                   getBalance()      { return balance; }
    public String                  getCurrency()     { return currency; }
    public WalletStatus            getStatus()       { return status; }
    public List<WalletTransaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public Instant                 getCreatedAt()    { return createdAt; }
    public Instant                 getUpdatedAt()    { return updatedAt; }

    public boolean isActive() { return status == WalletStatus.ACTIVE; }
}
