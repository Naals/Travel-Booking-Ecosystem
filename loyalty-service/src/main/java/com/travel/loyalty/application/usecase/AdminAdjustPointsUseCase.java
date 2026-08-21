package com.travel.loyalty.application.usecase;

import com.travel.loyalty.application.dto.request.AdminAdjustPointsRequest;
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
 * Support/admin goodwill credit or correction debit — mirrors
 * AdminAdjustWalletUseCase (wallet-service, Day 18) exactly. An
 * ADMIN_CREDIT deliberately counts toward lifetimePointsEarned (and
 * therefore tier) via earnPoints() — a goodwill gesture should help,
 * not just pad the spendable balance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAdjustPointsUseCase {

    private final LoyaltyAccountRepository repository;
    private final LoyaltyEventPublisher    eventPublisher;

    @Transactional
    public LoyaltyTransactionResponse execute(String userId, AdminAdjustPointsRequest request) {
        LoyaltyAccount account = repository.findById(LoyaltyAccountId.of(userId))
            .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", userId));

        Points  points   = Points.of(request.points());
        boolean isCredit = "CREDIT".equalsIgnoreCase(request.direction());

        var tx = isCredit
            ? account.earnPoints(points, LoyaltyTransactionType.ADMIN_CREDIT, null, request.reason())
            : account.redeemPoints(points, LoyaltyTransactionType.ADMIN_DEBIT, null, request.reason());

        LoyaltyAccount saved = repository.save(account);

        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Admin {} applied to loyalty account {}: points={} reason={}",
            request.direction(), userId, request.points(), request.reason());

        return LoyaltyTransactionResponse.from(tx);
    }
}
