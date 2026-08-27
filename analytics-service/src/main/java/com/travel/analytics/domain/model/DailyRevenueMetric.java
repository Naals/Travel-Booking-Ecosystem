package com.travel.analytics.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Platform-wide revenue for one (date, currency) bucket. Sourced from
 * payment-service's events (PaymentCompleted, RefundCompleted), not
 * booking-service's BookingConfirmedEvent — payment-service is the
 * authoritative source for money actually captured; BookingConfirmed's
 * totalAmount is just an echo of the same figure for the booking
 * record's own convenience.
 *
 * netRevenue() can legitimately be negative for a single day (e.g. a
 * slow day with one large refund from an earlier purchase) — that is
 * correct, not a bug, and no cross-field invariant enforces
 * refundedAmount &lt;= grossRevenue per bucket. Contrast Wallet's
 * balance (Day 18), which must never go negative — a different domain
 * with a different invariant.
 */
public final class DailyRevenueMetric {

    private final LocalDate date;
    private final String    currency;
    private BigDecimal      grossRevenue;
    private BigDecimal      refundedAmount;
    private Instant         updatedAt;

    private DailyRevenueMetric(LocalDate date, String currency, BigDecimal grossRevenue,
                               BigDecimal refundedAmount, Instant updatedAt) {
        this.date           = date;
        this.currency        = currency;
        this.grossRevenue     = grossRevenue;
        this.refundedAmount    = refundedAmount;
        this.updatedAt           = updatedAt;
    }

    public static DailyRevenueMetric initial(LocalDate date, String currency) {
        return new DailyRevenueMetric(date, currency, BigDecimal.ZERO, BigDecimal.ZERO, Instant.now());
    }

    public static DailyRevenueMetric reconstitute(LocalDate date, String currency,
                                                  BigDecimal grossRevenue, BigDecimal refundedAmount,
                                                  Instant updatedAt) {
        return new DailyRevenueMetric(date, currency, grossRevenue, refundedAmount, updatedAt);
    }

    public void addGrossRevenue(BigDecimal amount) {
        this.grossRevenue = this.grossRevenue.add(amount);
        this.updatedAt     = Instant.now();
    }

    public void addRefund(BigDecimal amount) {
        this.refundedAmount = this.refundedAmount.add(amount);
        this.updatedAt        = Instant.now();
    }

    public BigDecimal netRevenue() {
        return grossRevenue.subtract(refundedAmount);
    }

    public LocalDate  getDate()           { return date; }
    public String     getCurrency()       { return currency; }
    public BigDecimal getGrossRevenue()   { return grossRevenue; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public Instant     getUpdatedAt()      { return updatedAt; }
}
