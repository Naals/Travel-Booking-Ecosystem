package com.travel.loyalty.infrastructure.persistence;

import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import com.travel.loyalty.domain.repository.LoyaltyAccountRepository;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;
import com.travel.loyalty.infrastructure.persistence.mapper.LoyaltyAccountMapper;
import com.travel.loyalty.infrastructure.persistence.repository.LoyaltyAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoyaltyAccountRepositoryAdapter implements LoyaltyAccountRepository {

    private final LoyaltyAccountJpaRepository jpa;
    private final LoyaltyAccountMapper        mapper;

    @Override public LoyaltyAccount           save(LoyaltyAccount a)         { return mapper.toDomain(jpa.save(mapper.toEntity(a))); }
    @Override public Optional<LoyaltyAccount> findById(LoyaltyAccountId id)  { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public boolean                   existsById(LoyaltyAccountId id) { return jpa.existsById(id.getValue()); }
}
