package com.travel.fraud.domain.repository;

import com.travel.fraud.domain.aggregate.RiskProfile;
import com.travel.fraud.domain.valueobject.RiskProfileId;

import java.util.Optional;

public interface RiskProfileRepository {
    RiskProfile           save(RiskProfile profile);
    Optional<RiskProfile> findById(RiskProfileId id);
    boolean                existsById(RiskProfileId id);
}
