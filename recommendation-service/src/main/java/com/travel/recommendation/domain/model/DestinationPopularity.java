package com.travel.recommendation.domain.model;

import com.travel.recommendation.domain.valueobject.DestinationKey;

import java.time.Instant;
import java.util.Objects;

/**
 * Global, non-personalized trending signal for a destination.
 * Incremented only by completed trips (not by saved locations — a
 * private bookmark shouldn't move a public trending list). Same
 * "plain projection, not an aggregate" modeling as UserAffinity.
 */
public final class DestinationPopularity {

    private final DestinationKey destination;
    private long                  completedTripCount;
    private Instant                lastUpdatedAt;

    private DestinationPopularity(DestinationKey destination, long completedTripCount,
                                  Instant lastUpdatedAt) {
        this.destination         = Objects.requireNonNull(destination);
        this.completedTripCount  = completedTripCount;
        this.lastUpdatedAt        = lastUpdatedAt;
    }

    public static DestinationPopularity initial(DestinationKey destination) {
        return new DestinationPopularity(destination, 0L, Instant.now());
    }

    public static DestinationPopularity reconstitute(DestinationKey destination,
                                                     long completedTripCount,
                                                     Instant lastUpdatedAt) {
        return new DestinationPopularity(destination, completedTripCount, lastUpdatedAt);
    }

    /** No idempotency guard against duplicate delivery — see ADR-012. */
    public void increment() {
        this.completedTripCount++;
        this.lastUpdatedAt = Instant.now();
    }

    public DestinationKey getDestination()          { return destination; }
    public long            getCompletedTripCount()   { return completedTripCount; }
    public Instant          getLastUpdatedAt()        { return lastUpdatedAt; }
}
