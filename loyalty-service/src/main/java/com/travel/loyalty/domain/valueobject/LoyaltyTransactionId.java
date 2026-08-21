package com.travel.loyalty.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class LoyaltyTransactionId implements ValueObject {

    private final String value;

    private LoyaltyTransactionId(String value) {
        this.value = Objects.requireNonNull(value, "LoyaltyTransactionId must not be null");
    }

    public static LoyaltyTransactionId generate()       { return new LoyaltyTransactionId(UUID.randomUUID().toString()); }
    public static LoyaltyTransactionId of(String value) { return new LoyaltyTransactionId(value); }
    public String getValue()                              { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof LoyaltyTransactionId t && Objects.equals(value, t.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
