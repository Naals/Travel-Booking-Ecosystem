package com.travel.fraud.domain.service;

import com.travel.fraud.domain.model.RiskSnapshot;
import java.util.Optional;

/**
 * A single, independent tripwire. Implementations are pure — no
 * repository or external dependency — mirroring
 * ContentModerationPolicy (review-service, Day 16) and
 * TierCalculationPolicy (loyalty-service, Day 19).
 */
public interface FraudRule {

    /** Human-readable reason if triggered, empty otherwise. */
    Optional<String> evaluate(RiskSnapshot snapshot);

    /** Short identifier used as the alert's ruleName tag. */
    String name();
}
