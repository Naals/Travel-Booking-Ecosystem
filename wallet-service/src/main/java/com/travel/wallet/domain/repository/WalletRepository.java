package com.travel.wallet.domain.repository;

import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.valueobject.WalletId;

import java.util.Optional;

public interface WalletRepository {
    Wallet           save(Wallet wallet);
    Optional<Wallet> findById(WalletId id);
    boolean          existsById(WalletId id);
}
