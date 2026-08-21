package com.travel.loyalty.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * A non-negative whole-number point count. Simpler than Money (Booking,
 * Payment, Wallet, ...) — no currency, no decimal scale — since points
 * are always integral and platform-wide, not per-currency.
 */
public final class Points implements ValueObject {

    private final long value;

    private Points(long value) {
        if (value < 0)
            throw new DomainException("Points must not be negative", "INVALID_POINTS");
        this.value = value;
    }

    public static Points of(long value) { return new Points(value); }
    public static Points zero()          { return new Points(0L); }

    public long getValue() { return value; }

    public Points add(Points other)      { return new Points(this.value + other.value); }

    public Points subtract(Points other) {
        long result = this.value - other.value;
        if (result < 0)
            throw new DomainException("Subtraction would yield negative points", "INVALID_POINTS");
        return new Points(result);
    }

    public boolean isLessThan(Points other) { return this.value < other.value; }
    public boolean isZero()                  { return this.value == 0L; }

    @Override public boolean equals(Object o) {
        return o instanceof Points p && value == p.value;
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value + " pts"; }
}
