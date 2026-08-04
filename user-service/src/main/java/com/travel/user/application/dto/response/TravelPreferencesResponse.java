package com.travel.user.application.dto.response;

import com.travel.user.domain.model.DietaryRestriction;
import com.travel.user.domain.model.TravelPreferences;

import java.util.Set;
import java.util.stream.Collectors;

public record TravelPreferencesResponse(
    String      preferredCurrency,
    String      preferredLanguage,
    String      seatPreference,
    Set<String> dietaryRestrictions
) {
    public static TravelPreferencesResponse from(TravelPreferences p) {
        return new TravelPreferencesResponse(
            p.getPreferredCurrency(),
            p.getPreferredLanguage(),
            p.getSeatPreference().name(),
            p.getDietaryRestrictions().stream()
                .map(DietaryRestriction::name)
                .collect(Collectors.toSet())
        );
    }
}
