package com.travel.analytics.domain.repository;

import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.domain.model.DailyBookingMetric;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyBookingMetricRepository {
    DailyBookingMetric           save(DailyBookingMetric metric);
    Optional<DailyBookingMetric> findByDateAndBookingType(LocalDate date, BookingType type);
    List<DailyBookingMetric>     findByDateBetween(LocalDate from, LocalDate to);
    List<DailyBookingMetric>     findByDateBetweenAndBookingType(LocalDate from, LocalDate to, BookingType type);
}
