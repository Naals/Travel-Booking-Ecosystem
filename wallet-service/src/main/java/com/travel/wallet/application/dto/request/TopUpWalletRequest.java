package com.travel.wallet.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * idempotencyKey is caller-supplied (e.g. a UUID generated client-side)
 * rather than server-generated, so a retried request after a dropped
 * response is guaranteed to reference the same key — reuses the
 * idempotency vocabulary Payment (Day 8) established, though there it
 * was generated server-side; here it must come from the client since
 * this is the first write in the chain, not an internal retry step.
 */
public record TopUpWalletRequest(
    @NotNull @DecimalMin(value = "0.01", message = "Top-up amount must be positive")
    BigDecimal amount,

    @NotBlank(message = "idempotencyKey is required to make retries safe")
    String idempotencyKey
) {}
