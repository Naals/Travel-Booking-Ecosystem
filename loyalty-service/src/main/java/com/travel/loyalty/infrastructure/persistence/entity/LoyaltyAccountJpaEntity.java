package com.travel.loyalty.infrastructure.persistence.entity;

import com.travel.loyalty.domain.model.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loyalty_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoyaltyAccountJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "balance", nullable = false)
    private long balance;

    @Column(name = "lifetime_points_earned", nullable = false)
    private long lifetimePointsEarned;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private LoyaltyTier tier;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL,
        orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<LoyaltyTransactionJpaEntity> transactions = new ArrayList<>();

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
