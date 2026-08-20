package com.travel.wallet.infrastructure.persistence.entity;

import com.travel.wallet.domain.model.WalletTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_wallet_tx_user_id",   columnList = "user_id"),
    @Index(name = "idx_wallet_tx_reference", columnList = "reference_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletTransactionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private WalletJpaEntity wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private WalletTransactionType type;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "description")
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
