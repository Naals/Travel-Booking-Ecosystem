package com.travel.loyalty.application.dto.request;

import jakarta.validation.constraints.*;

public record AdminAdjustPointsRequest(
    @Min(1) long points,

    @NotBlank
    @Pattern(regexp = "CREDIT|DEBIT", message = "direction must be CREDIT or DEBIT")
    String direction,

    @NotBlank @Size(max = 500)
    String reason
) {}
