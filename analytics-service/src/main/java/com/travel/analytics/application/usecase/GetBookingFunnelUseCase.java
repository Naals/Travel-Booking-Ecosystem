package com.travel.analytics.application.usecase;

import com.travel.analytics.application.dto.response.BookingFunnelResponse;
import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.domain.model.DailyBookingMetric;
import com.travel.analytics.domain.repository.DailyBookingMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Sums a bounded list of daily rows in application code rather than a
 * SQL aggregate query — same reasoning wallet-service's
 * GetTransactionHistoryUseCase (Day 18) gave for in-memory pagination:
 * a date range of daily rows is naturally small (even a year is only
 * ~365 rows per type), so a complex aggregate query buys little here.
 */
@Service
@RequiredArgsConstructor
public class GetBookingFunnelUseCase {

    private static final int DEFAULT_RANGE_DAYS = 7;

    private final DailyBookingMetricRepository repository;

    @Transactional(readOnly = true)
    public BookingFunnelResponse execute(String bookingTypeParam, LocalDate from, LocalDate to) {
        LocalDate effectiveTo   = to != null ? to : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(DEFAULT_RANGE_DAYS - 1L);

        BookingType type = bookingTypeParam != null
            ? BookingType.valueOf(bookingTypeParam.toUpperCase()) : null;

        List<DailyBookingMetric> metrics = type != null
            ? repository.findByDateBetweenAndBookingType(effectiveFrom, effectiveTo, type)
            : repository.findByDateBetween(effectiveFrom, effectiveTo);

        long created   = metrics.stream().mapToLong(DailyBookingMetric::getCreatedCount).sum();
        long confirmed = metrics.stream().mapToLong(DailyBookingMetric::getConfirmedCount).sum();
        long completed = metrics.stream().mapToLong(DailyBookingMetric::getCompletedCount).sum();
        long cancelled = metrics.stream().mapToLong(DailyBookingMetric::getCancelledCount).sum();

        return BookingFunnelResponse.of(bookingTypeParam, effectiveFrom, effectiveTo,
            created, confirmed, completed, cancelled);
    }
}
