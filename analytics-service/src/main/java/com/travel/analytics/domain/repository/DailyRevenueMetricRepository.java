package com.travel.analytics.domain.repository;

import com.travel.analytics.domain.model.DailyRevenueMetric;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRevenueMetricRepository {
    DailyRevenueMetric           save(DailyRevenueMetric metric);
    Optional<DailyRevenueMetric> findByDateAndCurrency(LocalDate date, String currency);
    List<DailyRevenueMetric>     findByDateBetweenAndCurrency(LocalDate from, LocalDate to, String currency);
}
