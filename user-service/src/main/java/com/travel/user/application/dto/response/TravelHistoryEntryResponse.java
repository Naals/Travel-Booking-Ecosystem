package com.travel.user.application.dto.response;

import com.travel.user.domain.model.TravelHistoryEntry;
import java.time.Instant;

public record TravelHistoryEntryResponse(
    String  bookingId,
    String  resourceType,
    String  resourceName,
    Instant completedAt
) {
    public static TravelHistoryEntryResponse from(TravelHistoryEntry e) {
        return new TravelHistoryEntryResponse(
            e.getBookingId(), e.getResourceType(),
            e.getResourceName(), e.getCompletedAt());
    }
}
