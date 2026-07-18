package com.travel.booking.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable monetary amount.
 * Currency-aware — cannot mix USD and EUR in arithmetic.
 * Scale always enforced to 2 decimal places.
 */
public final class Money implements ValueObject {

    private final BigDecimal amount;
    private final String     currency;

    private Money(BigDecimal amount, String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("Amount must not be null or negative", "INVALID_MONEY");
        this.amount   = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
    }

    public static Money of(BigDecimal amount, String currency) { return new Money(amount, currency); }
    public static Money ofUSD(BigDecimal amount)               { return new Money(amount, "USD"); }
    public static Money zero(String currency)                  { return new Money(BigDecimal.ZERO, currency); }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        BigDecimal result = amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("Subtraction would yield negative amount", "INVALID_MONEY");
        return new Money(result, currency);
    }

    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() { return amount.compareTo(BigDecimal.ZERO) == 0; }

    public BigDecimal getAmount()  { return amount; }
    public String     getCurrency() { return currency; }

    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency))
            throw new DomainException(
                "Currency mismatch: " + currency + " vs " + other.currency,
                "CURRENCY_MISMATCH");
    }

    @Override public boolean equals(Object o) {
        return o instanceof Money m
            && amount.compareTo(m.amount) == 0
            && currency.equals(m.currency);
    }
    @Override public int    hashCode()  { return Objects.hash(amount, currency); }
    @Override public String toString()  { return amount + " " + currency; }
}
