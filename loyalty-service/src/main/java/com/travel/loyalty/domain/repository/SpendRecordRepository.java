package com.travel.loyalty.domain.repository;

import com.travel.loyalty.domain.model.SpendRecord;
import java.util.Optional;

public interface SpendRecordRepository {

    void save(SpendRecord record);

    boolean existsByBookingId(String bookingId);

    /**
     * Atomically consumes an unconsumed record for bookingId — the
     * relational equivalent of ReviewEligibilityRepository.tryConsume()
     * (review-service, Day 16), which used MongoDB's findAndModify.
     * See SpendRecordRepositoryAdapter for the conditional-UPDATE
     * mechanism this uses instead.
     */
    Optional<SpendRecord> tryConsume(String bookingId);

    /** No-op if no record exists — see BookingEventConsumer.onBookingCancelled(). */
    void voidIfExists(String bookingId);
}
