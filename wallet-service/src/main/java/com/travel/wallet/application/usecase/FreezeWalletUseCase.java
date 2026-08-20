package com.travel.wallet.application.usecase;

import com.travel.wallet.application.dto.request.FreezeWalletRequest;
import com.travel.wallet.application.dto.response.WalletResponse;
import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.repository.WalletRepository;
import com.travel.wallet.domain.valueobject.WalletId;
import com.travel.wallet.infrastructure.messaging.producer.WalletEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FreezeWalletUseCase {

    private final WalletRepository     repository;
    private final WalletEventPublisher eventPublisher;

    @Transactional
    public WalletResponse execute(String userId, FreezeWalletRequest request) {
        Wallet wallet = repository.findById(WalletId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId));

        wallet.freeze(request.reason());
        Wallet saved = repository.save(wallet);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        return WalletResponse.from(saved);
    }
}
