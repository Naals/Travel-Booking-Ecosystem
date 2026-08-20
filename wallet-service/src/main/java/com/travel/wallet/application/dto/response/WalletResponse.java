package com.travel.wallet.application.dto.response;

import com.travel.wallet.domain.aggregate.Wallet;
import java.math.BigDecimal;
import java.time.Instant;

public record WalletResponse(
    String     userId,
    BigDecimal balance,
    String     currency,
    String     status,
    Instant    createdAt
) {
    public static WalletResponse from(Wallet w) {
        return new WalletResponse(
            w.getId().getValue(), w.getBalance().getAmount(),
            w.getCurrency(), w.getStatus().name(), w.getCreatedAt());
    }
}
