package com.travel.fraud.domain.aggregate;

import com.travel.fraud.domain.event.FraudAlertRaisedEvent;
import com.travel.fraud.domain.event.RiskFlagClearedEvent;
import com.travel.fraud.domain.model.RiskSnapshot;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RiskProfile Aggregate Root.
 *
 * Tracks a bounded, time-windowed history of booking-created and
 * payment-failed timestamps per user, plus a lifetime completed-booking
 * count and a flagged/cleared state machine. toSnapshot() re-filters
 * by the rolling window at read time (not just at write time), so a
 * profile that hasn't been touched in a while is still evaluated
 * correctly even if opportunistic pruning has lagged.
 *
 * Follows the createdAt-preserving reconstitute() pattern established
 * by Conversation/Message (Day 17) — the private constructor takes
 * createdAt as a parameter.
 *
 * Storing a rolling window of raw timestamps in PostgreSQL is not how
 * a high-volume production velocity check would work — see ADR-013.
 */
public class RiskProfile extends AggregateRoot<RiskProfileId> {

    private static final Duration VELOCITY_WINDOW        = Duration.ofMinutes(60);
    private static final int      MAX_TRACKED_TIMESTAMPS  = 100; // defensive cap, independent of the time window

    private final Instant       accountCreatedAt;
    private final List<Instant> recentBookingTimestamps;
    private final List<Instant> recentPaymentFailureTimestamps;
    private long                lifetimeCompletedBookings;
    private boolean             flagged;
    private String              flagReason;
    private final Instant       createdAt;
    private Instant             updatedAt;

    private RiskProfile(RiskProfileId id, Instant accountCreatedAt, Instant createdAt) {
        super(id);
        this.accountCreatedAt               = accountCreatedAt;
        this.recentBookingTimestamps         = new ArrayList<>();
        this.recentPaymentFailureTimestamps  = new ArrayList<>();
        this.lifetimeCompletedBookings        = 0L;
        this.flagged                           = false;
        this.createdAt                          = createdAt;
        this.updatedAt                           = createdAt;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static RiskProfile provision(RiskProfileId userId, Instant accountCreatedAt) {
        return new RiskProfile(userId, accountCreatedAt, Instant.now());
    }

    public static RiskProfile reconstitute(RiskProfileId id, Instant accountCreatedAt,
                                           List<Instant> recentBookingTimestamps,
                                           List<Instant> recentPaymentFailureTimestamps,
                                           long lifetimeCompletedBookings,
                                           boolean flagged, String flagReason,
                                           Instant createdAt, Instant updatedAt) {
        RiskProfile p = new RiskProfile(id, accountCreatedAt, createdAt);
        p.recentBookingTimestamps.addAll(recentBookingTimestamps != null ? recentBookingTimestamps : List.of());
        p.recentPaymentFailureTimestamps.addAll(recentPaymentFailureTimestamps != null ? recentPaymentFailureTimestamps : List.of());
        p.lifetimeCompletedBookings = lifetimeCompletedBookings;
        p.flagged                   = flagged;
        p.flagReason                 = flagReason;
        p.updatedAt                   = updatedAt;
        return p;
    }

    // ── Recording signals ──────────────────────────────────────────────────────

    public void recordBookingCreated(Instant at) {
        prune(recentBookingTimestamps);
        recentBookingTimestamps.add(at);
        cap(recentBookingTimestamps);
        this.updatedAt = Instant.now();
    }

    public void recordPaymentFailed(Instant at) {
        prune(recentPaymentFailureTimestamps);
        recentPaymentFailureTimestamps.add(at);
        cap(recentPaymentFailureTimestamps);
        this.updatedAt = Instant.now();
    }

    public void recordBookingCompleted() {
        this.lifetimeCompletedBookings++;
        this.updatedAt = Instant.now();
    }

    public RiskSnapshot toSnapshot() {
        Instant cutoff = Instant.now().minus(VELOCITY_WINDOW);
        int bookingCount = (int) recentBookingTimestamps.stream().filter(t -> t.isAfter(cutoff)).count();
        int failureCount = (int) recentPaymentFailureTimestamps.stream().filter(t -> t.isAfter(cutoff)).count();
        return new RiskSnapshot(accountCreatedAt, bookingCount, failureCount, lifetimeCompletedBookings, flagged);
    }

    // ── Flag state machine ────────────────────────────────────────────────────

    /** Idempotent — FraudRuleEngine already short-circuits on alreadyFlagged, this is defense in depth. */
    public void raiseAlert(String ruleName, String reason) {
        if (flagged) return;
        this.flagged     = true;
        this.flagReason  = reason;
        this.updatedAt   = Instant.now();
        registerEvent(new FraudAlertRaisedEvent(getId().getValue(), ruleName, reason));
    }

    /**
     * Passive reaction to a wallet freeze that already happened —
     * whether triggered by our own alert via the wallet-service
     * auto-freeze consumer (this day), or by staff directly through
     * wallet-service's FreezeWalletUseCase (Day 18). No event raised;
     * this just stops future evaluation from re-triggering on a user
     * who is already frozen for an unrelated reason.
     */
    public void onWalletFrozen(String reason) {
        this.flagged = true;
        if (this.flagReason == null) this.flagReason = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Staff clears the flag after investigation. Also resets both
     * tracked windows — otherwise the very next booking or payment
     * event would immediately re-trigger the same rule that was just
     * manually cleared, since the underlying data is still present.
     */
    public void clearFlag(String staffId) {
        if (!flagged)
            throw new BusinessRuleViolationException("Risk profile is not flagged", "NOT_FLAGGED");
        this.flagged    = false;
        this.flagReason = null;
        recentBookingTimestamps.clear();
        recentPaymentFailureTimestamps.clear();
        this.updatedAt = Instant.now();
        registerEvent(new RiskFlagClearedEvent(getId().getValue(), staffId));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void prune(List<Instant> list) {
        Instant cutoff = Instant.now().minus(VELOCITY_WINDOW);
        list.removeIf(t -> t.isBefore(cutoff));
    }

    private void cap(List<Instant> list) {
        while (list.size() > MAX_TRACKED_TIMESTAMPS) list.remove(0);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Instant       getAccountCreatedAt()                { return accountCreatedAt; }
    public List<Instant> getRecentBookingTimestamps()          { return Collections.unmodifiableList(recentBookingTimestamps); }
    public List<Instant> getRecentPaymentFailureTimestamps()   { return Collections.unmodifiableList(recentPaymentFailureTimestamps); }
    public long           getLifetimeCompletedBookings()        { return lifetimeCompletedBookings; }
    public boolean         isFlagged()                            { return flagged; }
    public String           getFlagReason()                        { return flagReason; }
    public Instant           getCreatedAt()                         { return createdAt; }
    public Instant            getUpdatedAt()                         { return updatedAt; }
}
