package com.travel.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UpdateTravelPreferencesRequest(
    @NotBlank @Size(min = 3, max = 3) String preferredCurrency,
    @NotBlank @Size(min = 2, max = 2) String preferredLanguage,
    @NotBlank String seatPreference,
    Set<String> dietaryRestrictions
) {}
