package com.travel.fraud.application.service;

import com.travel.fraud.domain.aggregate.RiskProfile;
import com.travel.fraud.domain.repository.RiskProfileRepository;
import com.travel.fraud.domain.service.FraudRuleEngine;
import com.travel.fraud.infrastructure.messaging.producer.FraudEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Shared "evaluate against current rules, conditionally raise, persist,
 * publish" tail shared by RecordBookingCreatedUseCase and
 * RecordPaymentFailedUseCase — factored out rather than duplicated
 * across both, since the sequence is identical.
 */
@Service
@RequiredArgsConstructor
public class RiskEvaluationCoordinator {

    private final FraudRuleEngine       ruleEngine;
    private final RiskProfileRepository repository;
    private final FraudEventPublisher   eventPublisher;

    public RiskProfile evaluateAndPersist(RiskProfile profile) {
        ruleEngine.evaluate(profile.toSnapshot())
            .ifPresent(t -> profile.raiseAlert(t.ruleName(), t.reason()));

        RiskProfile saved = repository.save(profile);
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();
        return saved;
    }
}
