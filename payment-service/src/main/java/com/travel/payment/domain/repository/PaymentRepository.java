package com.travel.payment.domain.repository;

import com.travel.payment.domain.model.Payment;
import com.travel.payment.domain.valueobject.PaymentId;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment           save(Payment payment);
    Optional<Payment> findById(PaymentId id);
    Optional<Payment> findByBookingId(String bookingId);
    List<Payment>     findByUserId(String userId);
    boolean           existsByIdempotencyKey(String idempotencyKey);
}
