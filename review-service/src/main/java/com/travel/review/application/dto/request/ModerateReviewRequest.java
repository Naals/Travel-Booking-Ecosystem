package com.travel.review.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModerateReviewRequest(
    @NotBlank
    @Pattern(regexp = "APPROVE|REJECT", message = "decision must be APPROVE or REJECT")
    String decision,

    // Required only when decision = REJECT — enforced by
    // Review.reject() in the domain layer, not here.
    @Size(max = 500)
    String reason
) {}
