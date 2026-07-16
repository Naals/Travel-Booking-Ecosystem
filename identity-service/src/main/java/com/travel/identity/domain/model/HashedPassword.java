package com.travel.identity.domain.model;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;

public final class HashedPassword implements ValueObject {

    private final String hash;

    private HashedPassword(String hash) {
        this.hash = Objects.requireNonNull(hash, "Password hash must not be null");
    }

    public static HashedPassword ofHash(String hash) { return new HashedPassword(hash); }

    public String getHash() { return hash; }

    @Override
    public boolean equals(Object o) {
        return o instanceof HashedPassword hp && Objects.equals(hash, hp.hash);
    }

    @Override public int hashCode() { return Objects.hash(hash); }
}
