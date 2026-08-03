package com.travel.user.domain.model;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

public final class DisplayName implements ValueObject {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;

    private final String value;

    private DisplayName(String value) {
        if (value == null || value.isBlank())
            throw new DomainException("Display name must not be empty", "INVALID_DISPLAY_NAME");
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH)
            throw new DomainException(
                "Display name must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters",
                "INVALID_DISPLAY_NAME");
        this.value = trimmed;
    }

    public static DisplayName of(String value) { return new DisplayName(value); }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof DisplayName d && Objects.equals(value, d.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
