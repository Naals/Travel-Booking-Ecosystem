package com.travel.fraud.application.dto.response;

import com.travel.fraud.domain.aggregate.RiskProfile;
import java.time.Instant;

public record RiskProfileResponse(
    String  userId,
    Instant accountCreatedAt,
    int     recentBookingCount,
    int     recentPaymentFailureCount,
    long    lifetimeCompletedBookings,
    boolean flagged,
    String  flagReason
) {
    public static RiskProfileResponse from(RiskProfile p) {
        var snapshot = p.toSnapshot();
        return new RiskProfileResponse(
            p.getId().getValue(), p.getAccountCreatedAt(),
            snapshot.recentBookingCount(), snapshot.recentPaymentFailureCount(),
            p.getLifetimeCompletedBookings(), p.isFlagged(), p.getFlagReason());
    }
}
