package com.travel.user.application.usecase;

import com.travel.user.application.dto.response.UserProfileResponse;
import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.model.SavedLocationId;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.UserProfileRepository;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveSavedLocationUseCase {

    private final UserProfileRepository repository;

    @Transactional
    public UserProfileResponse execute(String userId, String savedLocationId) {
        UserProfile profile = repository.findById(UserId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));

        profile.removeSavedLocation(SavedLocationId.of(savedLocationId));

        UserProfile saved = repository.save(profile);
        return UserProfileResponse.from(saved);
    }
}
