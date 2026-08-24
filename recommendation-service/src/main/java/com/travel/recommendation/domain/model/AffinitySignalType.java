package com.travel.recommendation.domain.model;

/**
 * Weight reflects signal strength: an explicit bookmark is a mild hint
 * of interest; a completed trip is a revealed preference, weighted
 * more than 3x higher. Deliberately simple starting weights, not
 * tuned against any real data — see ADR-012.
 */
public enum AffinitySignalType {
    SAVED_LOCATION(3),
    COMPLETED_TRIP(10);

    private final int weight;

    AffinitySignalType(int weight) { this.weight = weight; }

    public int getWeight() { return weight; }
}
