package com.travel.fraud.application.usecase;

import com.travel.fraud.application.dto.response.RiskProfileResponse;
import com.travel.fraud.domain.repository.RiskProfileRepository;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRiskProfileUseCase {

    private final RiskProfileRepository repository;

    @Transactional(readOnly = true)
    public RiskProfileResponse execute(String userId) {
        return repository.findById(RiskProfileId.of(userId))
            .map(RiskProfileResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("RiskProfile", userId));
    }
}
