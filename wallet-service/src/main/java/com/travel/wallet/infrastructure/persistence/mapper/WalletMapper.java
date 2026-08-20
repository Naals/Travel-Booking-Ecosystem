package com.travel.wallet.infrastructure.persistence.mapper;

import com.travel.wallet.domain.aggregate.Wallet;
import com.travel.wallet.domain.model.WalletTransaction;
import com.travel.wallet.domain.valueobject.Money;
import com.travel.wallet.domain.valueobject.WalletId;
import com.travel.wallet.domain.valueobject.WalletTransactionId;
import com.travel.wallet.infrastructure.persistence.entity.WalletJpaEntity;
import com.travel.wallet.infrastructure.persistence.entity.WalletTransactionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletJpaEntity toEntity(Wallet w) {
        WalletJpaEntity entity = WalletJpaEntity.builder()
            .userId(w.getId().getValue())
            .balance(w.getBalance().getAmount())
            .currency(w.getCurrency())
            .status(w.getStatus())
            .createdAt(w.getCreatedAt())
            .updatedAt(w.getUpdatedAt())
            .build();

        w.getTransactions().forEach(t -> entity.getTransactions().add(
            WalletTransactionJpaEntity.builder()
                .id(t.getId().getValue())
                .wallet(entity)
                .type(t.getType())
                .amount(t.getAmount().getAmount())
                .balanceAfter(t.getBalanceAfter().getAmount())
                .referenceId(t.getReferenceId())
                .description(t.getDescription())
                .occurredAt(t.getOccurredAt())
                .build()));

        return entity;
    }

    public Wallet toDomain(WalletJpaEntity e) {
        var transactions = e.getTransactions().stream()
            .map(t -> new WalletTransaction(
                WalletTransactionId.of(t.getId()), t.getType(),
                Money.of(t.getAmount(), e.getCurrency()),
                Money.of(t.getBalanceAfter(), e.getCurrency()),
                t.getReferenceId(), t.getDescription(), t.getOccurredAt()))
            .toList();

        return Wallet.reconstitute(
            WalletId.of(e.getUserId()),
            Money.of(e.getBalance(), e.getCurrency()),
            e.getCurrency(),
            e.getStatus(),
            transactions,
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
