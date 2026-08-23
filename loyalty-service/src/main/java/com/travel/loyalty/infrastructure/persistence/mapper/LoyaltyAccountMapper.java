package com.travel.loyalty.infrastructure.persistence.mapper;

import com.travel.loyalty.domain.aggregate.LoyaltyAccount;
import com.travel.loyalty.domain.model.LoyaltyTransaction;
import com.travel.loyalty.domain.valueobject.LoyaltyAccountId;
import com.travel.loyalty.domain.valueobject.LoyaltyTransactionId;
import com.travel.loyalty.domain.valueobject.Points;
import com.travel.loyalty.infrastructure.persistence.entity.LoyaltyAccountJpaEntity;
import com.travel.loyalty.infrastructure.persistence.entity.LoyaltyTransactionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyAccountMapper {

    public LoyaltyAccountJpaEntity toEntity(LoyaltyAccount a) {
        LoyaltyAccountJpaEntity entity = LoyaltyAccountJpaEntity.builder()
            .userId(a.getId().getValue())
            .balance(a.getBalance().getValue())
            .lifetimePointsEarned(a.getLifetimePointsEarned().getValue())
            .tier(a.getTier())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();

        a.getTransactions().forEach(t -> entity.getTransactions().add(
            LoyaltyTransactionJpaEntity.builder()
                .id(t.getId().getValue())
                .account(entity)
                .type(t.getType())
                .points(t.getPoints().getValue())
                .balanceAfter(t.getBalanceAfter().getValue())
                .referenceId(t.getReferenceId())
                .description(t.getDescription())
                .occurredAt(t.getOccurredAt())
                .build()));

        return entity;
    }

    public LoyaltyAccount toDomain(LoyaltyAccountJpaEntity e) {
        var transactions = e.getTransactions().stream()
            .map(t -> new LoyaltyTransaction(
                LoyaltyTransactionId.of(t.getId()), t.getType(),
                Points.of(t.getPoints()), Points.of(t.getBalanceAfter()),
                t.getReferenceId(), t.getDescription(), t.getOccurredAt()))
            .toList();

        return LoyaltyAccount.reconstitute(
            LoyaltyAccountId.of(e.getUserId()),
            Points.of(e.getBalance()),
            Points.of(e.getLifetimePointsEarned()),
            e.getTier(),
            transactions,
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
