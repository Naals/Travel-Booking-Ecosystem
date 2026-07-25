package com.travel.flight.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class FlightId implements ValueObject {

    private final String value;

    private FlightId(String value) {
        this.value = Objects.requireNonNull(value, "FlightId must not be null");
    }

    public static FlightId generate()       { return new FlightId(UUID.randomUUID().toString()); }
    public static FlightId of(String value) { return new FlightId(value); }
    public String getValue()                 { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof FlightId f && Objects.equals(value, f.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
