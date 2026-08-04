package com.travel.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddSavedLocationRequest(
    @NotBlank @Size(max = 50) String label,
    @NotBlank String city,
    String country,
    Double latitude,
    Double longitude
) {}
