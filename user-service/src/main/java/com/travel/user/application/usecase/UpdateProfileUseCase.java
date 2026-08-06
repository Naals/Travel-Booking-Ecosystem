package com.travel.user.application.usecase;

import com.travel.user.application.dto.request.UpdateProfileRequest;
import com.travel.user.application.dto.response.UserProfileResponse;
import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.model.Bio;
import com.travel.user.domain.model.DisplayName;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.UserProfileRepository;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProfileUseCase {

    private final UserProfileRepository repository;

    @Transactional
    public UserProfileResponse execute(String userId, UpdateProfileRequest request) {
        UserProfile profile = repository.findById(UserId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));

        profile.updateProfile(
            DisplayName.of(request.displayName()),
            Bio.of(request.bio()),
            request.avatarUrl());

        UserProfile saved = repository.save(profile);
        log.info("Profile updated: {}", userId);
        return UserProfileResponse.from(saved);
    }
}
