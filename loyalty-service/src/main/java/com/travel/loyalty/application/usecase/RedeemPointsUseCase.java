package com.travel.loyalty.application.usecase;

import com.travel.loyalty.application.dto.request.RedeemPointsRequest;
import com.travel.loyalty.application.dto.response.LoyaltyTransactionResponse;
import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import com.travel.loyalty.domain.model.LoyaltyTransactionType;
import com.travel.loyalty.domain.repository.LoyaltyAccountRepository;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;
import com.travel.loyalty.domain.valueobject.Points;
import com.travel.loyalty.infrastructure.messaging.producer.LoyaltyEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Debits the ledger only — same deliberate scope limit as
 * TopUpWalletUseCase (wallet-service, Day 18): what redeemed points
 * are actually applied *to* (a wallet credit, a booking discount, a
 * loyalty-catalog reward) is real future work, not wired up here. This
 * service ships as a correct, self-contained ledger.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedeemPointsUseCase {

    private final LoyaltyAccountRepository repository;
    private final LoyaltyEventPublisher    eventPublisher;

    @Transactional
    public LoyaltyTransactionResponse execute(String userId, RedeemPointsRequest request) {
        LoyaltyAccount account = repository.findById(LoyaltyAccountId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", userId));

        var tx = account.redeemPoints(
            Points.of(request.points()), LoyaltyTransactionType.REDEEMED,
            null, request.description());

        LoyaltyAccount saved = repository.save(account);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Points redeemed: user={} points={} newBalance={}",
            userId, request.points(), saved.getBalance());

        return LoyaltyTransactionResponse.from(tx);
    }
}
