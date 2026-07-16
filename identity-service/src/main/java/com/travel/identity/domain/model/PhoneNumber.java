package com.travel.identity.domain.model;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;

public final class PhoneNumber implements ValueObject {

    private final String value;

    private PhoneNumber(String value) {
        this.value = Objects.requireNonNull(value, "PhoneNumber must not be null");
    }

    public static PhoneNumber of(String value) { return new PhoneNumber(value); }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        return o instanceof PhoneNumber pn && Objects.equals(value, pn.value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
}
