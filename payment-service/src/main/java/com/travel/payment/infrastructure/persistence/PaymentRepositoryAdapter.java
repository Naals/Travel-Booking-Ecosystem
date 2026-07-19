package com.travel.payment.infrastructure.persistence;

import com.travel.payment.domain.model.Payment;
import com.travel.payment.domain.repository.PaymentRepository;
import com.travel.payment.domain.valueobject.PaymentId;
import com.travel.payment.infrastructure.persistence.mapper.PaymentMapper;
import com.travel.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpa;
    private final PaymentMapper        mapper;

    @Override public Payment           save(Payment p)              { return mapper.toDomain(jpa.save(mapper.toEntity(p))); }
    @Override public Optional<Payment> findById(PaymentId id)       { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public Optional<Payment> findByBookingId(String bid)  { return jpa.findByBookingId(bid).map(mapper::toDomain); }
    @Override public List<Payment>     findByUserId(String uid)     { return jpa.findByUserId(uid).stream().map(mapper::toDomain).toList(); }
    @Override public boolean           existsByIdempotencyKey(String k) { return jpa.existsByIdempotencyKey(k); }
}
