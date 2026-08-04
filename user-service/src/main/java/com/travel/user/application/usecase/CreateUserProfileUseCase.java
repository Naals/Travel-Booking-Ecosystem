package com.travel.user.application.usecase;

import com.travel.user.domain.aggregate.UserProfile;
import com.travel.user.domain.model.DisplayName;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.UserProfileRepository;
import com.travel.user.infrastructure.messaging.producer.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a profile reactively in response to identity.user-registered.
 * Called exclusively by IdentityEventConsumer — there is no REST
 * endpoint for profile creation, since a profile's existence is a
 * direct consequence of registration, not an independent user action.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUserProfileUseCase {

    private final UserProfileRepository repository;
    private final UserEventPublisher    eventPublisher;

    @Transactional
    public void execute(String userId, String fullName) {
        UserId id = UserId.of(userId);

        if (repository.existsById(id)) {
            log.warn("Profile already exists for userId={} — skipping (idempotent)", userId);
            return;
        }

        UserProfile profile = UserProfile.create(id, DisplayName.of(fullName));
        UserProfile saved   = repository.save(profile);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("User profile created: {}", userId);
    }
}
