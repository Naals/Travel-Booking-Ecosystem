package com.travel.hotel.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class HotelId implements ValueObject {

    private final String value;

    private HotelId(String value) {
        this.value = Objects.requireNonNull(value, "HotelId must not be null");
    }

    public static HotelId generate()       { return new HotelId(UUID.randomUUID().toString()); }
    public static HotelId of(String value) { return new HotelId(value); }
    public String getValue()                { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof HotelId h && Objects.equals(value, h.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
