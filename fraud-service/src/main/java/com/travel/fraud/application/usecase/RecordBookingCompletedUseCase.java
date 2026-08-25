package com.travel.fraud.application.usecase;

import com.travel.fraud.domain.repository.RiskProfileRepository;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A completed booking is a positive signal, not evaluated against risk rules. */
@Service
@RequiredArgsConstructor
public class RecordBookingCompletedUseCase {

    private final RiskProfileRepository repository;

    @Transactional
    public void execute(String userId) {
        var profile = repository.findById(RiskProfileId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("RiskProfile", userId));
        profile.recordBookingCompleted();
        repository.save(profile);
    }
}
