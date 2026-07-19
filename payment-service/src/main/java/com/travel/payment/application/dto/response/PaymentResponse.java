package com.travel.payment.application.dto.response;

import com.travel.payment.domain.model.Payment;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
    String     paymentId,
    String     bookingId,
    String     userId,
    BigDecimal amount,
    String     currency,
    String     status,
    String     paymentMethod,
    String     externalPaymentId,
    String     failureReason,
    String     refundId,
    Instant    createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
            p.getId().getValue(),
            p.getBookingId(),
            p.getUserId(),
            p.getAmount().getAmount(),
            p.getAmount().getCurrency(),
            p.getStatus().name(),
            p.getPaymentMethod().name(),
            p.getExternalPaymentId(),
            p.getFailureReason(),
            p.getRefundId(),
            p.getCreatedAt()
        );
    }
}
