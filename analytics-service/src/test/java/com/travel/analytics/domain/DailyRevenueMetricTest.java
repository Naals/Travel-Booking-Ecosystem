package com.travel.analytics.domain;

import com.travel.analytics.domain.model.DailyRevenueMetric;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DailyRevenueMetric")
class DailyRevenueMetricTest {

    static final LocalDate DATE = LocalDate.of(2026, 8, 25);

    @Nested
    @DisplayName("Accumulation")
    class Accumulation {

        @Test @DisplayName("starts at zero gross and refunded")
        void startsAtZero() {
            var metric = DailyRevenueMetric.initial(DATE, "USD");
            assertThat(metric.getGrossRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(metric.getRefundedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test @DisplayName("addGrossRevenue accumulates across multiple calls")
        void accumulatesGross() {
            var metric = DailyRevenueMetric.initial(DATE, "USD");
            metric.addGrossRevenue(new BigDecimal("100.00"));
            metric.addGrossRevenue(new BigDecimal("50.50"));
            assertThat(metric.getGrossRevenue()).isEqualByComparingTo(new BigDecimal("150.50"));
        }

        @Test @DisplayName("netRevenue is gross minus refunded")
        void netRevenue() {
            var metric = DailyRevenueMetric.initial(DATE, "USD");
            metric.addGrossRevenue(new BigDecimal("200.00"));
            metric.addRefund(new BigDecimal("50.00"));
            assertThat(metric.netRevenue()).isEqualByComparingTo(new BigDecimal("150.00"));
        }
    }

    @Nested
    @DisplayName("Negative net revenue — intentionally allowed")
    class NegativeNet {

        @Test @DisplayName("a refund-only day produces negative netRevenue, not an error")
        void refundOnlyDayIsNegative() {
            var metric = DailyRevenueMetric.initial(DATE, "USD");
            metric.addRefund(new BigDecimal("75.00")); // no gross revenue this day
            assertThat(metric.netRevenue()).isEqualByComparingTo(new BigDecimal("-75.00"));
        }
    }
}
