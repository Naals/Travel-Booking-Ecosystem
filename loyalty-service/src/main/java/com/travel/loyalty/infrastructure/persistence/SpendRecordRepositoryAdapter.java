package com.travel.loyalty.infrastructure.persistence;

import com.travel.loyalty.domain.model.SpendRecord;
import com.travel.loyalty.domain.repository.SpendRecordRepository;
import com.travel.loyalty.infrastructure.persistence.entity.SpendRecordJpaEntity;
import com.travel.loyalty.infrastructure.persistence.repository.SpendRecordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpendRecordRepositoryAdapter implements SpendRecordRepository {

    private final SpendRecordJpaRepository jpa;

    @Override
    public void save(SpendRecord record) {
        jpa.save(SpendRecordJpaEntity.builder()
            .bookingId(record.getBookingId())
            .userId(record.getUserId())
            .amount(record.getAmount())
            .currency(record.getCurrency())
            .consumed(false)
            .recordedAt(record.getRecordedAt())
            .build());
    }

    @Override
    public boolean existsByBookingId(String bookingId) {
        return jpa.existsById(bookingId);
    }

    @Override
    @Transactional
    public Optional<SpendRecord> tryConsume(String bookingId) {
        // Fetched before the UPDATE purely to read the immutable
        // amount/currency/userId fields — the atomicity guarantee comes
        // from markConsumed()'s row count, not from this SELECT. If two
        // threads race, both may read here, but only one markConsumed()
        // call can return 1; the other correctly returns empty below.
        var existing = jpa.findById(bookingId);
        if (existing.isEmpty()) return Optional.empty();

        int updated = jpa.markConsumed(bookingId);
        if (updated == 0) return Optional.empty(); // already consumed by a concurrent call

        var e = existing.get();
        return Optional.of(SpendRecord.of(e.getBookingId(), e.getUserId(), e.getAmount(), e.getCurrency()));
    }

    @Override
    public void voidIfExists(String bookingId) {
        jpa.deleteById(bookingId);
    }
}
