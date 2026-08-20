package com.travel.wallet.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class WalletTransactionId implements ValueObject {

    private final String value;

    private WalletTransactionId(String value) {
        this.value = Objects.requireNonNull(value, "WalletTransactionId must not be null");
    }

    public static WalletTransactionId generate()       { return new WalletTransactionId(UUID.randomUUID().toString()); }
    public static WalletTransactionId of(String value) { return new WalletTransactionId(value); }
    public String getValue()                             { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof WalletTransactionId t && Objects.equals(value, t.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
