package com.travel.fraud.domain.model;

import java.time.Duration;
import java.time.Instant;

/**
 * Immutable countable state passed into FraudRule implementations,
 * decoupling rules from RiskProfileRepository the same way
 * RecommendationEngine's inputs (Day 20) decoupled ranking from
 * repository access.
 */
public record RiskSnapshot(
    Instant accountCreatedAt,
    int     recentBookingCount,
    int     recentPaymentFailureCount,
    long    lifetimeCompletedBookings,
    boolean alreadyFlagged
) {
    public Duration accountAge() {
        return Duration.between(accountCreatedAt, Instant.now());
    }
}
