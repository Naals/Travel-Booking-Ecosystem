package com.travel.loyalty.domain.repository;

import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;

import java.util.Optional;

public interface LoyaltyAccountRepository {
    LoyaltyAccount           save(LoyaltyAccount account);
    Optional<LoyaltyAccount> findById(LoyaltyAccountId id);
    boolean                   existsById(LoyaltyAccountId id);
}
