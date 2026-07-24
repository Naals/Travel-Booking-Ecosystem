package com.travel.hotel.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class RoomId implements ValueObject {

    private final String value;

    private RoomId(String value) {
        this.value = Objects.requireNonNull(value, "RoomId must not be null");
    }

    public static RoomId generate()       { return new RoomId(UUID.randomUUID().toString()); }
    public static RoomId of(String value) { return new RoomId(value); }
    public String getValue()               { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof RoomId r && Objects.equals(value, r.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
