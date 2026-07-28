package com.travel.vehicle.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class VehicleId implements ValueObject {

    private final String value;

    private VehicleId(String value) {
        this.value = Objects.requireNonNull(value, "VehicleId must not be null");
    }

    public static VehicleId generate()       { return new VehicleId(UUID.randomUUID().toString()); }
    public static VehicleId of(String value) { return new VehicleId(value); }
    public String getValue()                  { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof VehicleId v && Objects.equals(value, v.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
