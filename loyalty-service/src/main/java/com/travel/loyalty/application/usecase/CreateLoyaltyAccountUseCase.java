package com.travel.loyalty.application.usecase;

import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import com.travel.loyalty.domain.repository.LoyaltyAccountRepository;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;
import com.travel.loyalty.infrastructure.messaging.producer.LoyaltyEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Third reactive-provisioning use case in this platform, after
 * CreateUserProfileUseCase (Day 15) and CreateWalletUseCase (Day 18) —
 * same idempotency guard, same shape.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateLoyaltyAccountUseCase {

    private final LoyaltyAccountRepository repository;
    private final LoyaltyEventPublisher    eventPublisher;

    @Transactional
    public void execute(String userId) {
        LoyaltyAccountId id = LoyaltyAccountId.of(userId);

        if (repository.existsById(id)) {
            log.warn("Loyalty account already exists for userId={} — skipping (idempotent)", userId);
            return;
        }

        LoyaltyAccount account = LoyaltyAccount.provision(id);
        LoyaltyAccount saved   = repository.save(account);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Loyalty account provisioned: {}", userId);
    }
}
