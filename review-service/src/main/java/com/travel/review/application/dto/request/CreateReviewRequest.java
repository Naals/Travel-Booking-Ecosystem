package com.travel.review.application.dto.request;

import jakarta.validation.constraints.*;

public record CreateReviewRequest(
    @NotBlank(message = "bookingId is required")
    String bookingId,

    @Min(1) @Max(5)
    int rating,

    @NotBlank @Size(max = 100)
    String title,

    @NotBlank @Size(min = 10, max = 3000)
    String body
) {}
