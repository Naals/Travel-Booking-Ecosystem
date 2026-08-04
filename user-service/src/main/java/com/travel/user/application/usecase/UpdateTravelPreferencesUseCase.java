package com.travel.user.application.usecase;

import com.travel.user.application.dto.request.UpdateTravelPreferencesRequest;
import com.travel.user.application.dto.response.UserProfileResponse;
import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.model.DietaryRestriction;
import com.travel.user.domain.model.SeatPreference;
import com.travel.user.domain.model.TravelPreferences;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.UserProfileRepository;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateTravelPreferencesUseCase {

    private final UserProfileRepository repository;

    @Transactional
    public UserProfileResponse execute(String userId, UpdateTravelPreferencesRequest request) {
        UserProfile profile = repository.findById(UserId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));

        Set<DietaryRestriction> restrictions = request.dietaryRestrictions() != null
            ? request.dietaryRestrictions().stream()
            .map(DietaryRestriction::valueOf)
            .collect(Collectors.toSet())
            : Set.of();

        TravelPreferences preferences = TravelPreferences.of(
            request.preferredCurrency(),
            request.preferredLanguage(),
            SeatPreference.valueOf(request.seatPreference()),
            restrictions);

        profile.updateTravelPreferences(preferences);

        UserProfile saved = repository.save(profile);
        return UserProfileResponse.from(saved);
    }
}
