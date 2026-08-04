package com.travel.user.infrastructure.persistence.mapper;

import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.model.*;
import com.travel.user.infrastructure.persistence.entity.SavedLocationJpaEntity;
import com.travel.user.infrastructure.persistence.entity.UserProfileJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserProfileMapper {

    public UserProfileJpaEntity toEntity(UserProfile p) {
        UserProfileJpaEntity entity = UserProfileJpaEntity.builder()
            .userId(p.getId().getValue())
            .displayName(p.getDisplayName().getValue())
            .bio(p.getBio().getValue())
            .avatarUrl(p.getAvatarUrl())
            .preferredCurrency(p.getTravelPreferences().getPreferredCurrency())
            .preferredLanguage(p.getTravelPreferences().getPreferredLanguage())
            .seatPreference(p.getTravelPreferences().getSeatPreference())
            .dietaryRestrictions(p.getTravelPreferences().getDietaryRestrictions())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();

        p.getSavedLocations().forEach(loc -> entity.getSavedLocations().add(
            SavedLocationJpaEntity.builder()
                .id(loc.getId().getValue())
                .userProfile(entity)
                .label(loc.getLabel())
                .city(loc.getCity())
                .country(loc.getCountry())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build()));

        return entity;
    }

    public UserProfile toDomain(UserProfileJpaEntity e) {
        List<SavedLocation> locations = e.getSavedLocations().stream()
            .map(l -> new SavedLocation(
                SavedLocationId.of(l.getId()), l.getLabel(), l.getCity(),
                l.getCountry(), l.getLatitude(), l.getLongitude()))
            .toList();

        TravelPreferences preferences = TravelPreferences.of(
            e.getPreferredCurrency(), e.getPreferredLanguage(),
            e.getSeatPreference(), e.getDietaryRestrictions());

        return UserProfile.reconstitute(
            UserId.of(e.getUserId()),
            DisplayName.of(e.getDisplayName()),
            Bio.of(e.getBio()),
            e.getAvatarUrl(),
            preferences,
            locations,
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
