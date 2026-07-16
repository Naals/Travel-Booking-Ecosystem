package com.travel.identity.domain.model;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class UserId implements ValueObject {

    private final String value;

    private UserId(String value) {
        this.value = Objects.requireNonNull(value, "UserId must not be null");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserId uid && Objects.equals(value, uid.value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
