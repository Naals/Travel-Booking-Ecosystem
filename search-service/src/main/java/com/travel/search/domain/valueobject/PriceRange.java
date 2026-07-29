package com.travel.search.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.math.BigDecimal;
import java.util.Objects;

public final class PriceRange implements ValueObject {

    private final BigDecimal min;
    private final BigDecimal max;

    private PriceRange(BigDecimal min, BigDecimal max) {
        if (min != null && min.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("Minimum price must not be negative", "INVALID_PRICE_RANGE");
        if (min != null && max != null && min.compareTo(max) > 0)
            throw new DomainException("Minimum price must not exceed maximum price", "INVALID_PRICE_RANGE");
        this.min = min;
        this.max = max;
    }

    public static PriceRange of(BigDecimal min, BigDecimal max) {
        return new PriceRange(min, max);
    }

    public BigDecimal getMin() { return min; }
    public BigDecimal getMax() { return max; }

    @Override public boolean equals(Object o) {
        return o instanceof PriceRange p
            && Objects.equals(min, p.min) && Objects.equals(max, p.max);
    }
    @Override public int hashCode() { return Objects.hash(min, max); }
}
