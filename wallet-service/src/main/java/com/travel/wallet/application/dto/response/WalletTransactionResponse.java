package com.travel.wallet.application.dto.response;

import com.travel.wallet.domain.model.WalletTransaction;
import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransactionResponse(
    String     transactionId,
    String     type,
    BigDecimal amount,
    BigDecimal balanceAfter,
    String     description,
    Instant    occurredAt
) {
    public static WalletTransactionResponse from(WalletTransaction t) {
        return new WalletTransactionResponse(
            t.getId().getValue(), t.getType().name(),
            t.getAmount().getAmount(), t.getBalanceAfter().getAmount(),
            t.getDescription(), t.getOccurredAt());
    }
}
