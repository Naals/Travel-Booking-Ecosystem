package com.travel.property.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class PropertyId implements ValueObject {

    private final String value;

    private PropertyId(String value) {
        this.value = Objects.requireNonNull(value, "PropertyId must not be null");
    }

    public static PropertyId generate()       { return new PropertyId(UUID.randomUUID().toString()); }
    public static PropertyId of(String value) { return new PropertyId(value); }
    public String getValue()                   { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof PropertyId p && Objects.equals(value, p.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
