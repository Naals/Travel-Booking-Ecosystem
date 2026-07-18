package com.travel.booking.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class BookingId implements ValueObject {

    private final String value;

    private BookingId(String value) {
        this.value = Objects.requireNonNull(value, "BookingId must not be null");
    }

    public static BookingId generate()        { return new BookingId(UUID.randomUUID().toString()); }
    public static BookingId of(String value)  { return new BookingId(value); }
    public String getValue()                   { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof BookingId b && Objects.equals(value, b.value);
    }
    @Override public int hashCode()  { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
