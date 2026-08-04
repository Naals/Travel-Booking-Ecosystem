package com.travel.user.application.usecase;

import com.travel.user.application.dto.request.AddSavedLocationRequest;
import com.travel.user.application.dto.response.UserProfileResponse;
import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.UserProfileRepository;
import com.travel.user.infrastructure.messaging.producer.UserEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddSavedLocationUseCase {

    private final UserProfileRepository repository;
    private final UserEventPublisher    eventPublisher;

    @Transactional
    public UserProfileResponse execute(String userId, AddSavedLocationRequest request) {
        UserProfile profile = repository.findById(UserId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile", userId));

        profile.addSavedLocation(
            request.label(), request.city(), request.country(),
            request.latitude(), request.longitude());

        UserProfile saved = repository.save(profile);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Saved location '{}' added for user {}", request.label(), userId);
        return UserProfileResponse.from(saved);
    }
}
