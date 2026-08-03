package com.travel.user.domain.model;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * Short free-text profile bio. Unlike DisplayName, blank is valid —
 * an empty bio just means the user hasn't written one yet.
 */
public final class Bio implements ValueObject {

    private static final int MAX_LENGTH = 500;

    private final String value; // never null internally — empty string when unset

    private Bio(String value) {
        String normalized = value != null ? value.trim() : "";
        if (normalized.length() > MAX_LENGTH)
            throw new DomainException(
                "Bio must not exceed " + MAX_LENGTH + " characters", "INVALID_BIO");
        this.value = normalized;
    }

    public static Bio of(String value) { return new Bio(value); }
    public static Bio empty()          { return new Bio(null); }

    public String  getValue() { return value; }
    public boolean isEmpty()  { return value.isEmpty(); }

    @Override public boolean equals(Object o) {
        return o instanceof Bio b && Objects.equals(value, b.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
