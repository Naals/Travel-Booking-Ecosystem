package com.travel.fraud.application.usecase;

import com.travel.fraud.application.service.RiskEvaluationCoordinator;
import com.travel.fraud.domain.repository.RiskProfileRepository;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordPaymentFailedUseCase {

    private final RiskProfileRepository     repository;
    private final RiskEvaluationCoordinator coordinator;

    @Transactional
    public void execute(String userId, Instant at) {
        var profile = repository.findById(RiskProfileId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("RiskProfile", userId));

        profile.recordPaymentFailed(at);
        var saved = coordinator.evaluateAndPersist(profile);

        if (saved.isFlagged()) {
            log.warn("Risk profile flagged: user={} reason={}", userId, saved.getFlagReason());
        }
    }
}
