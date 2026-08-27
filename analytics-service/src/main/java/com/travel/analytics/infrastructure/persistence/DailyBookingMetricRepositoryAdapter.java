package com.travel.analytics.infrastructure.persistence;

import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.domain.model.DailyBookingMetric;
import com.travel.analytics.domain.repository.DailyBookingMetricRepository;
import com.travel.analytics.infrastructure.persistence.mapper.DailyBookingMetricMapper;
import com.travel.analytics.infrastructure.persistence.repository.DailyBookingMetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DailyBookingMetricRepositoryAdapter implements DailyBookingMetricRepository {

    private final DailyBookingMetricJpaRepository jpa;
    private final DailyBookingMetricMapper        mapper;

    @Override
    public DailyBookingMetric save(DailyBookingMetric m) {
        Long existingId = jpa.findByDateAndBookingType(m.getDate(), m.getBookingType())
            .map(DailyBookingMetricJpaEntity -> DailyBookingMetricJpaEntity.getId())
            .orElse(null);
        var saved = jpa.save(mapper.toEntity(m, existingId));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DailyBookingMetric> findByDateAndBookingType(LocalDate date, BookingType type) {
        return jpa.findByDateAndBookingType(date, type).map(mapper::toDomain);
    }

    @Override
    public List<DailyBookingMetric> findByDateBetween(LocalDate from, LocalDate to) {
        return jpa.findByDateBetween(from, to).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<DailyBookingMetric> findByDateBetweenAndBookingType(LocalDate from, LocalDate to, BookingType type) {
        return jpa.findByDateBetweenAndBookingType(from, to, type).stream().map(mapper::toDomain).toList();
    }
}
