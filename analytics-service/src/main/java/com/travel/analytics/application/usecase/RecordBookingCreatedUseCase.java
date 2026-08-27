package com.travel.analytics.application.usecase;

import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.domain.model.DailyBookingMetric;
import com.travel.analytics.domain.repository.BookingTypeLookupRepository;
import com.travel.analytics.domain.repository.DailyBookingMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RecordBookingCreatedUseCase {

    private final BookingTypeLookupRepository  lookupRepository;
    private final DailyBookingMetricRepository metricRepository;

    @Transactional
    public void execute(String bookingId, BookingType type, LocalDate date) {
        // Record the correlation first — Confirmed/Cancelled will need it.
        lookupRepository.record(bookingId, type);

        DailyBookingMetric metric = metricRepository.findByDateAndBookingType(date, type)
            .orElseGet(() -> DailyBookingMetric.initial(date, type));
        metric.incrementCreated();
        metricRepository.save(metric);
    }
}
