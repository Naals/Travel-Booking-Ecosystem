package com.travel.payment.infrastructure.persistence.entity;

import com.travel.payment.domain.valueobject.PaymentMethod;
import com.travel.payment.domain.valueobject.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_booking_id",       columnList = "booking_id"),
    @Index(name = "idx_payments_user_id",          columnList = "user_id"),
    @Index(name = "idx_payments_status",           columnList = "status"),
    @Index(name = "idx_payments_idempotency_key",  columnList = "idempotency_key", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "booking_id", nullable = false) private String bookingId;
    @Column(name = "user_id",    nullable = false) private String userId;

    @Column(name = "amount",   nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "external_payment_id")          private String  externalPaymentId;
    @Column(name = "idempotency_key", unique = true) private String idempotencyKey;
    @Column(name = "failure_reason")               private String  failureReason;
    @Column(name = "refund_id")                    private String  refundId;

    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false)                    private Instant updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = Instant.now(); }
}
