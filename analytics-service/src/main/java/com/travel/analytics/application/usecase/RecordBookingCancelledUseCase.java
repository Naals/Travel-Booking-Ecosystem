package com.travel.analytics.application.usecase;

import com.travel.analytics.domain.model.DailyBookingMetric;
import com.travel.analytics.domain.repository.BookingTypeLookupRepository;
import com.travel.analytics.domain.repository.DailyBookingMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordBookingCancelledUseCase {

    private final BookingTypeLookupRepository  lookupRepository;
    private final DailyBookingMetricRepository metricRepository;

    @Transactional
    public void execute(String bookingId, LocalDate date) {
        lookupRepository.findByBookingId(bookingId).ifPresentOrElse(type -> {
            DailyBookingMetric metric = metricRepository.findByDateAndBookingType(date, type)
                .orElseGet(() -> DailyBookingMetric.initial(date, type));
            metric.incrementCancelled();
            metricRepository.save(metric);
        }, () -> log.warn("No BookingTypeLookup entry for booking {} — cancelled count not recorded", bookingId));
    }
}
