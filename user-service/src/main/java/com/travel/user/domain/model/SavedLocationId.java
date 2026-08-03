package com.travel.user.domain.model;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class SavedLocationId implements ValueObject {

    private final String value;

    private SavedLocationId(String value) {
        this.value = Objects.requireNonNull(value, "SavedLocationId must not be null");
    }

    public static SavedLocationId generate()       { return new SavedLocationId(UUID.randomUUID().toString()); }
    public static SavedLocationId of(String value) { return new SavedLocationId(value); }
    public String getValue()                        { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof SavedLocationId s && Objects.equals(value, s.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
