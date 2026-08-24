package com.travel.recommendation.domain.model;

import com.travel.recommendation.domain.valueobject.DestinationKey;

import java.time.Instant;
import java.util.Objects;

/**
 * A user's accumulated interest in a single destination.
 *
 * Not a DDD aggregate root — a CQRS read/write projection with no
 * invariants to protect and no domain events to raise, the same
 * modeling choice search-service made for SearchDocument (Day 14):
 * "not an aggregate root ... enforces only basic construction
 * validity." This class goes one step further and raises no events
 * at all, since nothing downstream needs to react to "affinity changed."
 */
public final class UserAffinity {

    private final String        userId;
    private final DestinationKey destination;
    private long                 score;
    private Instant               lastSignalAt;

    private UserAffinity(String userId, DestinationKey destination,
                         long score, Instant lastSignalAt) {
        this.userId       = Objects.requireNonNull(userId);
        this.destination  = Objects.requireNonNull(destination);
        this.score          = score;
        this.lastSignalAt    = lastSignalAt;
    }

    public static UserAffinity initial(String userId, DestinationKey destination) {
        return new UserAffinity(userId, destination, 0L, Instant.now());
    }

    public static UserAffinity reconstitute(String userId, DestinationKey destination,
                                            long score, Instant lastSignalAt) {
        return new UserAffinity(userId, destination, score, lastSignalAt);
    }

    /** No idempotency guard against duplicate delivery — see ADR-012. */
    public void recordSignal(AffinitySignalType type) {
        this.score        += type.getWeight();
        this.lastSignalAt   = Instant.now();
    }

    public String        getUserId()      { return userId; }
    public DestinationKey getDestination() { return destination; }
    public long            getScore()       { return score; }
    public Instant          getLastSignalAt() { return lastSignalAt; }
}
