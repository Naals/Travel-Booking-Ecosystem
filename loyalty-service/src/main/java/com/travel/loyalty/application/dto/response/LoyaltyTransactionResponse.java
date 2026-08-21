package com.travel.loyalty.application.dto.response;

import com.travel.loyalty.domain.model.LoyaltyTransaction;
import java.time.Instant;

public record LoyaltyTransactionResponse(
    String  transactionId,
    String  type,
    long    points,
    long    balanceAfter,
    String  description,
    Instant occurredAt
) {
    public static LoyaltyTransactionResponse from(LoyaltyTransaction t) {
        return new LoyaltyTransactionResponse(
            t.getId().getValue(), t.getType().name(),
            t.getPoints().getValue(), t.getBalanceAfter().getValue(),
            t.getDescription(), t.getOccurredAt());
    }
}
