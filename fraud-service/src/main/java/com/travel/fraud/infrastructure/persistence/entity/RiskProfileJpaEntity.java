package com.travel.fraud.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskProfileJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "account_created_at", nullable = false)
    private Instant accountCreatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_profile_booking_timestamps",
        joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "occurred_at")
    @Builder.Default
    private List<Instant> recentBookingTimestamps = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "risk_profile_payment_failure_timestamps",
        joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "occurred_at")
    @Builder.Default
    private List<Instant> recentPaymentFailureTimestamps = new ArrayList<>();

    @Column(name = "lifetime_completed_bookings", nullable = false)
    private long lifetimeCompletedBookings;

    @Column(name = "flagged", nullable = false)
    private boolean flagged;

    @Column(name = "flag_reason")
    private String flagReason;

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
