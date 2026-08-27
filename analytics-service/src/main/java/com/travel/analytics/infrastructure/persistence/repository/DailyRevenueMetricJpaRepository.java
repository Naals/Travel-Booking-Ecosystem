package com.travel.analytics.infrastructure.persistence.repository;

import com.travel.analytics.infrastructure.persistence.entity.DailyRevenueMetricJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRevenueMetricJpaRepository extends JpaRepository<DailyRevenueMetricJpaEntity, Long> {
    Optional<DailyRevenueMetricJpaEntity> findByDateAndCurrency(LocalDate date, String currency);
    List<DailyRevenueMetricJpaEntity>     findByDateBetweenAndCurrency(LocalDate from, LocalDate to, String currency);
}
