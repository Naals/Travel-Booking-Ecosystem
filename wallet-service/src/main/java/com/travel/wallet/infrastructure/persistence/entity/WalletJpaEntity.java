package com.travel.wallet.infrastructure.persistence.entity;

import com.travel.wallet.domain.model.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WalletStatus status;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL,
        orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<WalletTransactionJpaEntity> transactions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}
