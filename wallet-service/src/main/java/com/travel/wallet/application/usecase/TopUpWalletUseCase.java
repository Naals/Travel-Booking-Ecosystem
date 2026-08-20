package com.travel.wallet.application.usecase;

import com.travel.wallet.application.dto.request.TopUpWalletRequest;
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
 * Credits the wallet directly for the requested amount.
 *
 * Deliberate simplification, documented fully in ADR-010: in
 * production this would first charge a card through payment-service's
 * Stripe integration (Day 8) and only credit the wallet once that
 * payment succeeded. Wiring that up correctly needs its own mini-saga
 * — real work deliberately left for a future pass rather than rushed
 * into today's scope.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopUpWalletUseCase {

    private final WalletRepository     repository;
    private final WalletEventPublisher eventPublisher;

    @Transactional
    public WalletTransactionResponse execute(String userId, TopUpWalletRequest request) {
        Wallet wallet = repository.findById(WalletId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId));

        var tx = wallet.credit(
            Money.of(request.amount(), wallet.getCurrency()),
            WalletTransactionType.TOPUP,
            request.idempotencyKey(),
            "Wallet top-up");

        Wallet saved = repository.save(wallet);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Wallet topped up: user={} amount={} newBalance={}",
            userId, request.amount(), saved.getBalance());

        return WalletTransactionResponse.from(tx);
    }
}
