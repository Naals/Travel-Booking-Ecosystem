package com.travel.wallet.application.usecase;

import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.repository.WalletRepository;
import com.travel.wallet.domain.valueobject.WalletId;
import com.travel.wallet.infrastructure.messaging.producer.WalletEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a wallet reactively in response to identity.user-registered.
 * Mirrors user-service's CreateUserProfileUseCase (Day 15) — same
 * idempotency guard, same log message shape, same "no direct REST
 * creation endpoint" design.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateWalletUseCase {

    private final WalletRepository     repository;
    private final WalletEventPublisher eventPublisher;

    @Transactional
    public void execute(String userId) {
        WalletId id = WalletId.of(userId);

        if (repository.existsById(id)) {
            log.warn("Wallet already exists for userId={} — skipping (idempotent)", userId);
            return;
        }

        Wallet wallet = Wallet.provision(id);
        Wallet saved  = repository.save(wallet);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Wallet provisioned: {}", userId);
    }
}
