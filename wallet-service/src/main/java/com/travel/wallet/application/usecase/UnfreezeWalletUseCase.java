package com.travel.wallet.application.usecase;

import com.travel.wallet.application.dto.response.WalletResponse;
import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.repository.WalletRepository;
import com.travel.wallet.domain.valueobject.WalletId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnfreezeWalletUseCase {

    private final WalletRepository repository;

    @Transactional
    public WalletResponse execute(String userId) {
        Wallet wallet = repository.findById(WalletId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId));

        wallet.unfreeze();
        Wallet saved = repository.save(wallet);

        return WalletResponse.from(saved);
    }
}
