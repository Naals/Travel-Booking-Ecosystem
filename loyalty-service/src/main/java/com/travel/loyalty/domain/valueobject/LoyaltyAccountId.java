package com.travel.loyalty.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Always equals the userId minted by identity-service — the third
 * identifier in this platform to follow that convention (no
 * .generate()), after UserId (user-service, Day 15) and WalletId
 * (wallet-service, Day 18). Not repeating the rationale here; see
 * either of those classes' Javadoc.
 */
public final class LoyaltyAccountId implements ValueObject {

    private final String value;

    private LoyaltyAccountId(String value) {
        this.value = Objects.requireNonNull(value, "LoyaltyAccountId must not be null");
    }

    public static LoyaltyAccountId of(String value) { return new LoyaltyAccountId(value); }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof LoyaltyAccountId l && Objects.equals(value, l.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
