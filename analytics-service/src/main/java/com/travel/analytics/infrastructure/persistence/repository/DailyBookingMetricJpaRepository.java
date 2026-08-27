package com.travel.analytics.infrastructure.persistence.repository;

import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.infrastructure.persistence.entity.DailyBookingMetricJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyBookingMetricJpaRepository extends JpaRepository<DailyBookingMetricJpaEntity, Long> {
    Optional<DailyBookingMetricJpaEntity> findByDateAndBookingType(LocalDate date, BookingType type);
    List<DailyBookingMetricJpaEntity>     findByDateBetween(LocalDate from, LocalDate to);
    List<DailyBookingMetricJpaEntity>     findByDateBetweenAndBookingType(LocalDate from, LocalDate to, BookingType type);
}
