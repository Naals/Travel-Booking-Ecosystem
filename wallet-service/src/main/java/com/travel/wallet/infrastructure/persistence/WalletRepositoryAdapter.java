package com.travel.wallet.infrastructure.persistence;

import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.repository.WalletRepository;
import com.travel.wallet.domain.valueobject.WalletId;
import com.travel.wallet.infrastructure.persistence.mapper.WalletMapper;
import com.travel.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepository {

    private final WalletJpaRepository jpa;
    private final WalletMapper        mapper;

    @Override public Wallet           save(Wallet w)          { return mapper.toDomain(jpa.save(mapper.toEntity(w))); }
    @Override public Optional<Wallet> findById(WalletId id)   { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public boolean          existsById(WalletId id) { return jpa.existsById(id.getValue()); }
}
