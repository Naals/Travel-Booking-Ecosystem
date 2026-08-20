package com.travel.wallet.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Wallet identifier. Always equals the userId minted by
 * identity-service at registration — mirrors user-service's UserId
 * (Day 15) exactly, including the deliberate absence of a .generate()
 * factory. See that class's Javadoc for the full rationale; not
 * repeated here.
 */
public final class WalletId implements ValueObject {

    private final String value;

    private WalletId(String value) {
        this.value = Objects.requireNonNull(value, "WalletId must not be null");
    }

    public static WalletId of(String value) { return new WalletId(value); }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof WalletId w && Objects.equals(value, w.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
