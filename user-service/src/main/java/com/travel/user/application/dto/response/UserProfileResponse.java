package com.travel.user.application.dto.response;

import com.travel.user.domain.aggregate.UserProfile;

import java.time.Instant;
import java.util.List;

public record UserProfileResponse(
    String                      userId,
    String                      displayName,
    String                      bio,
    String                      avatarUrl,
    TravelPreferencesResponse   preferences,
    List<SavedLocationResponse> savedLocations,
    Instant                     createdAt
) {
    public static UserProfileResponse from(UserProfile p) {
        return new UserProfileResponse(
            p.getId().getValue(),
            p.getDisplayName().getValue(),
            p.getBio().getValue(),
            p.getAvatarUrl(),
            TravelPreferencesResponse.from(p.getTravelPreferences()),
            p.getSavedLocations().stream().map(SavedLocationResponse::from).toList(),
            p.getCreatedAt()
        );
    }
}
