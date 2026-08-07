package com.travel.review.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FlagReviewRequest(
    @NotBlank @Size(max = 500)
    String reason
) {}
