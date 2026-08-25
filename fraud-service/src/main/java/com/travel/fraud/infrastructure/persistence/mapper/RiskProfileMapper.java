package com.travel.fraud.infrastructure.persistence.mapper;

import com.travel.fraud.domain.aggregate.RiskProfile;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import com.travel.fraud.infrastructure.persistence.entity.RiskProfileJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RiskProfileMapper {

    public RiskProfileJpaEntity toEntity(RiskProfile p) {
        return RiskProfileJpaEntity.builder()
            .userId(p.getId().getValue())
            .accountCreatedAt(p.getAccountCreatedAt())
            .recentBookingTimestamps(new ArrayList<>(p.getRecentBookingTimestamps()))
            .recentPaymentFailureTimestamps(new ArrayList<>(p.getRecentPaymentFailureTimestamps()))
            .lifetimeCompletedBookings(p.getLifetimeCompletedBookings())
            .flagged(p.isFlagged())
            .flagReason(p.getFlagReason())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }

    public RiskProfile toDomain(RiskProfileJpaEntity e) {
        return RiskProfile.reconstitute(
            RiskProfileId.of(e.getUserId()),
            e.getAccountCreatedAt(),
            e.getRecentBookingTimestamps(),
            e.getRecentPaymentFailureTimestamps(),
            e.getLifetimeCompletedBookings(),
            e.isFlagged(),
            e.getFlagReason(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
