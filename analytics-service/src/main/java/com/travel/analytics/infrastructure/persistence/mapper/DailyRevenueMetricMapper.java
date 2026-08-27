package com.travel.analytics.infrastructure.persistence.mapper;

import com.travel.analytics.domain.model.DailyRevenueMetric;
import com.travel.analytics.infrastructure.persistence.entity.DailyRevenueMetricJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DailyRevenueMetricMapper {

    public DailyRevenueMetricJpaEntity toEntity(DailyRevenueMetric m, Long existingId) {
        return DailyRevenueMetricJpaEntity.builder()
            .id(existingId)
            .date(m.getDate())
            .currency(m.getCurrency())
            .grossRevenue(m.getGrossRevenue())
            .refundedAmount(m.getRefundedAmount())
            .updatedAt(m.getUpdatedAt())
            .build();
    }

    public DailyRevenueMetric toDomain(DailyRevenueMetricJpaEntity e) {
        return DailyRevenueMetric.reconstitute(
            e.getDate(), e.getCurrency(), e.getGrossRevenue(), e.getRefundedAmount(), e.getUpdatedAt());
    }
}
