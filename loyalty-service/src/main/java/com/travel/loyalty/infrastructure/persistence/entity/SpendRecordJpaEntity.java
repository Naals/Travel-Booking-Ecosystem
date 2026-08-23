package com.travel.loyalty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * bookingId as the primary key (not a generated UUID) — the natural
 * key doubles as the idempotency guard for RecordSpendUseCase's
 * existsByBookingId() check, the same choice ReviewEligibilityDocument
 * made using bookingId as its Mongo _id (review-service, Day 16).
 */
@Entity
@Table(name = "spend_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpendRecordJpaEntity {

    @Id
    @Column(name = "booking_id", nullable = false, updatable = false)
    private String bookingId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "consumed", nullable = false)
    private boolean consumed;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @PrePersist void prePersist() {
        if (recordedAt == null) recordedAt = Instant.now();
    }
}
