package com.travel.loyalty.application.dto.response;

import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import java.time.Instant;

public record LoyaltyAccountResponse(
    String  userId,
    long    balance,
    long    lifetimePointsEarned,
    String  tier,
    Instant createdAt
) {
    public static LoyaltyAccountResponse from(LoyaltyAccount a) {
        return new LoyaltyAccountResponse(
            a.getId().getValue(), a.getBalance().getValue(),
            a.getLifetimePointsEarned().getValue(), a.getTier().name(),
            a.getCreatedAt());
    }
}
