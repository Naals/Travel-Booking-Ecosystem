package com.travel.wallet.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FreezeWalletRequest(
    @NotBlank @Size(max = 500)
    String reason
) {}
