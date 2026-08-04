package com.travel.user.infrastructure.persistence.mapper;

import com.travel.user.domain.model.TravelHistoryEntry;
import com.travel.user.infrastructure.persistence.entity.TravelHistoryEntryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TravelHistoryMapper {

    public TravelHistoryEntryJpaEntity toEntity(TravelHistoryEntry e) {
        return TravelHistoryEntryJpaEntity.builder()
            .userId(e.getUserId())
            .bookingId(e.getBookingId())
            .resourceType(e.getResourceType())
            .resourceName(e.getResourceName())
            .completedAt(e.getCompletedAt())
            .build();
    }

    public TravelHistoryEntry toDomain(TravelHistoryEntryJpaEntity e) {
        return TravelHistoryEntry.of(
            e.getUserId(), e.getBookingId(), e.getResourceType(),
            e.getResourceName(), e.getCompletedAt());
    }
}
