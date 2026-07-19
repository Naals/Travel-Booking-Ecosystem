package com.travel.payment.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class PaymentId implements ValueObject {

    private final String value;

    private PaymentId(String value) {
        this.value = Objects.requireNonNull(value, "PaymentId must not be null");
    }

    public static PaymentId generate()       { return new PaymentId(UUID.randomUUID().toString()); }
    public static PaymentId of(String value) { return new PaymentId(value); }
    public String getValue()                  { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof PaymentId p && Objects.equals(value, p.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
