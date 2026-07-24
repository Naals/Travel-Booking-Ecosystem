package com.travel.hotel.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements ValueObject {

    private final BigDecimal amount;
    private final String     currency;

    private Money(BigDecimal amount, String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("Amount must not be null or negative", "INVALID_MONEY");
        this.amount   = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = Objects.requireNonNull(currency);
    }

    public static Money of(BigDecimal amount, String currency) { return new Money(amount, currency); }
    public static Money ofUSD(BigDecimal amount)               { return new Money(amount, "USD"); }

    public BigDecimal getAmount()   { return amount; }
    public String     getCurrency() { return currency; }

    public Money multiply(long factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Money m
            && amount.compareTo(m.amount) == 0 && currency.equals(m.currency);
    }
    @Override public int    hashCode() { return Objects.hash(amount, currency); }
    @Override public String toString() { return amount + " " + currency; }
}
