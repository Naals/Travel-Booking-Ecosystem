package com.travel.analytics.infrastructure.persistence.mapper;

import com.travel.analytics.domain.model.DailyBookingMetric;
import com.travel.analytics.infrastructure.persistence.entity.DailyBookingMetricJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DailyBookingMetricMapper {

    public DailyBookingMetricJpaEntity toEntity(DailyBookingMetric m, Long existingId) {
        return DailyBookingMetricJpaEntity.builder()
            .id(existingId)
            .date(m.getDate())
            .bookingType(m.getBookingType())
            .createdCount(m.getCreatedCount())
            .confirmedCount(m.getConfirmedCount())
            .completedCount(m.getCompletedCount())
            .cancelledCount(m.getCancelledCount())
            .updatedAt(m.getUpdatedAt())
            .build();
    }

    public DailyBookingMetric toDomain(DailyBookingMetricJpaEntity e) {
        return DailyBookingMetric.reconstitute(
            e.getDate(), e.getBookingType(), e.getCreatedCount(), e.getConfirmedCount(),
            e.getCompletedCount(), e.getCancelledCount(), e.getUpdatedAt());
    }
}
