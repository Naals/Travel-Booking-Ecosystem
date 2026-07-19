package com.travel.payment.infrastructure.persistence.repository;

import com.travel.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, String> {
    Optional<PaymentJpaEntity> findByBookingId(String bookingId);
    List<PaymentJpaEntity>     findByUserId(String userId);
    boolean                    existsByIdempotencyKey(String idempotencyKey);
}
