package com.travel.fraud.infrastructure.persistence;

import com.travel.fraud.domain.aggregate.RiskProfile;
import com.travel.fraud.domain.repository.RiskProfileRepository;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import com.travel.fraud.infrastructure.persistence.mapper.RiskProfileMapper;
import com.travel.fraud.infrastructure.persistence.repository.RiskProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RiskProfileRepositoryAdapter implements RiskProfileRepository {

    private final RiskProfileJpaRepository jpa;
    private final RiskProfileMapper        mapper;

    @Override public RiskProfile           save(RiskProfile p)          { return mapper.toDomain(jpa.save(mapper.toEntity(p))); }
    @Override public Optional<RiskProfile> findById(RiskProfileId id)   { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public boolean                existsById(RiskProfileId id) { return jpa.existsById(id.getValue()); }
}
