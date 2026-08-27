package com.travel.analytics.infrastructure.persistence;

import com.travel.analytics.domain.model.DailyRevenueMetric;
import com.travel.analytics.domain.repository.DailyRevenueMetricRepository;
import com.travel.analytics.infrastructure.persistence.mapper.DailyRevenueMetricMapper;
import com.travel.analytics.infrastructure.persistence.repository.DailyRevenueMetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DailyRevenueMetricRepositoryAdapter implements DailyRevenueMetricRepository {

    private final DailyRevenueMetricJpaRepository jpa;
    private final DailyRevenueMetricMapper        mapper;

    @Override
    public DailyRevenueMetric save(DailyRevenueMetric m) {
        Long existingId = jpa.findByDateAndCurrency(m.getDate(), m.getCurrency())
            .map(e -> e.getId())
            .orElse(null);
        var saved = jpa.save(mapper.toEntity(m, existingId));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DailyRevenueMetric> findByDateAndCurrency(LocalDate date, String currency) {
        return jpa.findByDateAndCurrency(date, currency).map(mapper::toDomain);
    }

    @Override
    public List<DailyRevenueMetric> findByDateBetweenAndCurrency(LocalDate from, LocalDate to, String currency) {
        return jpa.findByDateBetweenAndCurrency(from, to, currency).stream().map(mapper::toDomain).toList();
    }
}
