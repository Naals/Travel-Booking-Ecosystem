package com.travel.wallet.application.usecase;

import com.travel.wallet.application.dto.response.WalletResponse;
import com.travel.wallet.domain.repository.WalletRepository;
import com.travel.wallet.domain.valueobject.WalletId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns 404 rather than lazily provisioning — same rationale as
 * user-service's GetUserProfileUseCase (Day 15): a missing wallet
 * shortly after registration usually means Kafka consumer lag, and
 * that should be visible, not silently papered over.
 */
@Service
@RequiredArgsConstructor
public class GetWalletUseCase {

    private final WalletRepository repository;

    @Transactional(readOnly = true)
    public WalletResponse execute(String userId) {
        return repository.findById(WalletId.of(userId))
            .map(WalletResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId));
    }
}
