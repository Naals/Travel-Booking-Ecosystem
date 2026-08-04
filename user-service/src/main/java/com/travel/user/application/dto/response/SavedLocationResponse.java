package com.travel.user.application.dto.response;

import com.travel.user.domain.model.SavedLocation;

public record SavedLocationResponse(
    String savedLocationId,
    String label,
    String city,
    String country,
    Double latitude,
    Double longitude
) {
    public static SavedLocationResponse from(SavedLocation l) {
        return new SavedLocationResponse(
            l.getId().getValue(), l.getLabel(), l.getCity(),
            l.getCountry(), l.getLatitude(), l.getLongitude());
    }
}
