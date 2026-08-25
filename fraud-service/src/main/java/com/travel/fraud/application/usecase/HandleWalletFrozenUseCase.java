package com.travel.fraud.application.usecase;

import com.travel.fraud.domain.repository.RiskProfileRepository;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reacts to wallet-service's WalletFrozenEvent (Day 18) — the consumer
 * that event's Javadoc said didn't exist yet. Tolerant of a missing
 * profile: a wallet can be frozen for a user who has never triggered
 * any fraud-relevant event (e.g. staff froze it for an unrelated
 * reason before this user ever booked anything).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HandleWalletFrozenUseCase {

    private final RiskProfileRepository repository;

    @Transactional
    public void execute(String userId, String reason) {
        repository.findById(RiskProfileId.of(userId)).ifPresentOrElse(
            profile -> {
                profile.onWalletFrozen(reason);
                repository.save(profile);
            },
            () -> log.debug("WalletFrozen for user {} with no risk profile — skipping", userId)
        );
    }
}
