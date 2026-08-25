package com.travel.fraud.application.usecase;

import com.travel.fraud.application.dto.response.RiskProfileResponse;
import com.travel.fraud.domain.repository.RiskProfileRepository;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import com.travel.fraud.infrastructure.messaging.producer.FraudEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClearRiskFlagUseCase {

    private final RiskProfileRepository repository;
    private final FraudEventPublisher   eventPublisher;

    @Transactional
    public RiskProfileResponse execute(String userId, String staffId) {
        var profile = repository.findById(RiskProfileId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("RiskProfile", userId));

        profile.clearFlag(staffId);
        var saved = repository.save(profile);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Risk flag cleared: user={} staff={}", userId, staffId);
        return RiskProfileResponse.from(saved);
    }
}
