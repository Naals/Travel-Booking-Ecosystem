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
public class RecordBookingConfirmedUseCase {

    private final BookingTypeLookupRepository  lookupRepository;
    private final DailyBookingMetricRepository metricRepository;

    /**
     * If the BookingTypeLookup entry is missing — a genuine, rare
     * possibility since BOOKING_CREATED and BOOKING_CONFIRMED are
     * separate Kafka topics with no cross-topic ordering guarantee —
     * this confirmation is logged and dropped rather than guessed at
     * or bucketed under a synthetic type value. Same graceful-
     * degradation philosophy as recommendation-service's
     * AffinitySignalConsumer (Day 20) when a destination lookup is
     * missing.
     */
    @Transactional
    public void execute(String bookingId, LocalDate date) {
        lookupRepository.findByBookingId(bookingId).ifPresentOrElse(type -> {
            DailyBookingMetric metric = metricRepository.findByDateAndBookingType(date, type)
                .orElseGet(() -> DailyBookingMetric.initial(date, type));
            metric.incrementConfirmed();
            metricRepository.save(metric);
        }, () -> log.warn("No BookingTypeLookup entry for booking {} — confirmed count not recorded", bookingId));
    }
}
