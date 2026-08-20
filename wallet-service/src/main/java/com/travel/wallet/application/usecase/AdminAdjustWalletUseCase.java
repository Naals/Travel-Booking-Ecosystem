package com.travel.wallet.application.usecase;

import com.travel.wallet.application.dto.request.AdjustWalletRequest;
import com.travel.wallet.application.dto.response.WalletTransactionResponse;
import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.model.WalletTransactionType;
import com.travel.wallet.domain.repository.WalletRepository;
import com.travel.wallet.domain.valueobject.Money;
import com.travel.wallet.domain.valueobject.WalletId;
import com.travel.wallet.infrastructure.messaging.producer.WalletEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Support/admin-triggered credit or debit — e.g. goodwill compensation
 * for a service failure, or correcting an erroneous top-up. No
 * idempotencyKey required: unlike TopUpWalletUseCase, this is invoked
 * directly by a trusted staff member through an internal tool, not
 * retried automatically by a client.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAdjustWalletUseCase {

    private final WalletRepository     repository;
    private final WalletEventPublisher eventPublisher;

    @Transactional
    public WalletTransactionResponse execute(String userId, AdjustWalletRequest request) {
        Wallet wallet = repository.findById(WalletId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId));

        Money   amount   = Money.of(request.amount(), wallet.getCurrency());
        boolean isCredit = "CREDIT".equalsIgnoreCase(request.direction());

        var tx = isCredit
            ? wallet.credit(amount, WalletTransactionType.ADMIN_CREDIT, null, request.reason())
            : wallet.debit(amount, WalletTransactionType.ADMIN_DEBIT, null, request.reason());

        Wallet saved = repository.save(wallet);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Admin {} applied to wallet {}: amount={} reason={}",
            request.direction(), userId, request.amount(), request.reason());

        return WalletTransactionResponse.from(tx);
    }
}
