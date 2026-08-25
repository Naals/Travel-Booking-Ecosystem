package com.travel.fraud.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Always equals identity-service's userId — the fourth identifier in
 * this platform to follow the no-.generate() convention, after UserId
 * (Day 15), WalletId (Day 18), and LoyaltyAccountId (Day 19).
 */
public final class RiskProfileId implements ValueObject {

    private final String value;

    private RiskProfileId(String value) {
        this.value = Objects.requireNonNull(value, "RiskProfileId must not be null");
    }

    public static RiskProfileId of(String value) { return new RiskProfileId(value); }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof RiskProfileId r && Objects.equals(value, r.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
