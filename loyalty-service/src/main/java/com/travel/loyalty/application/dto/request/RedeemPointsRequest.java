package com.travel.loyalty.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedeemPointsRequest(
    @Min(1) long points,

    @NotBlank @Size(max = 500)
    String description
) {}
