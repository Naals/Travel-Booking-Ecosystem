package com.travel.payment.infrastructure.persistence.mapper;

import com.travel.payment.domain.model.Payment;
import com.travel.payment.domain.valueobject.*;
import com.travel.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentJpaEntity toEntity(Payment p) {
        return PaymentJpaEntity.builder()
            .id(p.getId().getValue())
            .bookingId(p.getBookingId())
            .userId(p.getUserId())
            .amount(p.getAmount().getAmount())
            .currency(p.getAmount().getCurrency())
            .status(p.getStatus())
            .paymentMethod(p.getPaymentMethod())
            .externalPaymentId(p.getExternalPaymentId())
            .idempotencyKey(p.getIdempotencyKey())
            .failureReason(p.getFailureReason())
            .refundId(p.getRefundId())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }

    public Payment toDomain(PaymentJpaEntity e) {
        return Payment.reconstitute(
            PaymentId.of(e.getId()),
            e.getBookingId(),
            e.getUserId(),
            Money.of(e.getAmount(), e.getCurrency()),
            e.getStatus(),
            e.getPaymentMethod(),
            e.getExternalPaymentId(),
            e.getIdempotencyKey(),
            e.getFailureReason(),
            e.getRefundId(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
