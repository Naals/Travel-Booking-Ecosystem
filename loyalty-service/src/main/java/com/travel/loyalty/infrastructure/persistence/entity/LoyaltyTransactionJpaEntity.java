package com.travel.loyalty.infrastructure.persistence.entity;

import com.travel.loyalty.domain.model.LoyaltyTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "loyalty_transactions", indexes = {
    @Index(name = "idx_loyalty_tx_user_id",   columnList = "user_id"),
    @Index(name = "idx_loyalty_tx_reference", columnList = "reference_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoyaltyTransactionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private LoyaltyAccountJpaEntity account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LoyaltyTransactionType type;

    @Column(name = "points", nullable = false)
    private long points;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "description")
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
