package com.travel.user.application.usecase;

import com.travel.user.application.dto.response.UserProfileResponse;
import com.travel.user.domain.repository.UserProfileRepository;
import com.travel.user.domain.model.UserId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns 404 if a profile doesn't exist yet, rather than lazily
 * creating one — a missing profile shortly after registration usually
 * means identity.user-registered hasn't been consumed yet (Kafka lag)
 * or failed processing. Surfacing that as a clear 404 keeps the gap
 * observable instead of an on-demand create silently masking it.
 */
@Service
@RequiredArgsConstructor
public class GetUserProfileUseCase {

    private final UserProfileRepository repository;

    @Transactional(readOnly = true)
    public UserProfileResponse execute(String userId) {
        return repository.findById(UserId.of(userId))
            .map(UserProfileResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));
    }
}
