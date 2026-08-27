package com.travel.analytics.application.usecase;

import com.travel.analytics.application.dto.response.RevenueSummaryResponse;
import com.travel.analytics.domain.model.DailyRevenueMetric;
import com.travel.analytics.domain.repository.DailyRevenueMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GetRevenueSummaryUseCase {

    private static final int DEFAULT_RANGE_DAYS = 7;

    private final DailyRevenueMetricRepository repository;

    /** currency is mandatory — summing across currencies isn't meaningful. */
    @Transactional(readOnly = true)
    public RevenueSummaryResponse execute(String currency, LocalDate from, LocalDate to) {
        LocalDate effectiveTo   = to != null ? to : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(DEFAULT_RANGE_DAYS - 1L);

        var metrics = repository.findByDateBetweenAndCurrency(effectiveFrom, effectiveTo, currency);

        BigDecimal gross    = metrics.stream().map(DailyRevenueMetric::getGrossRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refunded = metrics.stream().map(DailyRevenueMetric::getRefundedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RevenueSummaryResponse(currency, effectiveFrom, effectiveTo, gross, refunded, gross.subtract(refunded));
    }
}
