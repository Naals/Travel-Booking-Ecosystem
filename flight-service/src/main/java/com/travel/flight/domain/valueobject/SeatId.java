package com.travel.flight.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class SeatId implements ValueObject {

    private final String value;

    private SeatId(String value) {
        this.value = Objects.requireNonNull(value, "SeatId must not be null");
    }

    public static SeatId generate()       { return new SeatId(UUID.randomUUID().toString()); }
    public static SeatId of(String value) { return new SeatId(value); }
    public String getValue()               { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof SeatId s && Objects.equals(value, s.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
