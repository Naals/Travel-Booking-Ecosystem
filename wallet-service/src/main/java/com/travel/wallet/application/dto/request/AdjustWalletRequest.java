package com.travel.wallet.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AdjustWalletRequest(
    @NotNull @DecimalMin(value = "0.01")
    BigDecimal amount,

    @NotBlank
    @Pattern(regexp = "CREDIT|DEBIT", message = "direction must be CREDIT or DEBIT")
    String direction,

    @NotBlank @Size(max = 500)
    String reason
) {}
