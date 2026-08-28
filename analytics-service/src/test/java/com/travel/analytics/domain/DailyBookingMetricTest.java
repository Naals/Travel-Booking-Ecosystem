package com.travel.analytics.domain;

import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.domain.model.DailyBookingMetric;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DailyBookingMetric")
class DailyBookingMetricTest {

    static final LocalDate DATE = LocalDate.of(2026, 8, 25);

    @Test @DisplayName("starts at zero across all counts")
    void startsAtZero() {
        var metric = DailyBookingMetric.initial(DATE, BookingType.PROPERTY);
        assertThat(metric.getCreatedCount()).isZero();
        assertThat(metric.getConfirmedCount()).isZero();
        assertThat(metric.getCompletedCount()).isZero();
        assertThat(metric.getCancelledCount()).isZero();
    }

    @Test @DisplayName("each increment method affects only its own counter")
    void incrementsIndependently() {
        var metric = DailyBookingMetric.initial(DATE, BookingType.HOTEL);
        metric.incrementCreated();
        metric.incrementCreated();
        metric.incrementConfirmed();

        assertThat(metric.getCreatedCount()).isEqualTo(2L);
        assertThat(metric.getConfirmedCount()).isEqualTo(1L);
        assertThat(metric.getCompletedCount()).isZero();
        assertThat(metric.getCancelledCount()).isZero();
    }

    @Test @DisplayName("preserves date and bookingType across reconstitution")
    void preservesIdentity() {
        var metric = DailyBookingMetric.reconstitute(
            DATE, BookingType.FLIGHT, 10, 8, 6, 2, java.time.Instant.now());
        assertThat(metric.getDate()).isEqualTo(DATE);
        assertThat(metric.getBookingType()).isEqualTo(BookingType.FLIGHT);
    }
}
