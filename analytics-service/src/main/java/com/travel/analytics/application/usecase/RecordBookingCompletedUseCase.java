package com.travel.analytics.application.usecase;

import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.domain.model.DailyBookingMetric;
import com.travel.analytics.domain.repository.DailyBookingMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Unlike Confirmed/Cancelled, no BookingTypeLookup lookup is needed —
 * BookingCompletedEvent has carried bookingType directly since its
 * Day 15 enrichment.
 */
@Service
@RequiredArgsConstructor
public class RecordBookingCompletedUseCase {

    private final DailyBookingMetricRepository repository;

    @Transactional
    public void execute(BookingType type, LocalDate date) {
        DailyBookingMetric metric = repository.findByDateAndBookingType(date, type)
            .orElseGet(() -> DailyBookingMetric.initial(date, type));
        metric.incrementCompleted();
        repository.save(metric);
    }
}
