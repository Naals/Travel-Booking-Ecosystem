package com.travel.user.application.usecase;

import com.travel.user.domain.model.TravelHistoryEntry;
import com.travel.user.domain.model.UserId;
import com.travel.user.domain.repository.TravelHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Projects a completed booking into this user's travel history.
 * Called by BookingEventConsumer on booking.booking-completed.
 *
 * Idempotency: the (userId, bookingId) existence check plus a unique
 * DB constraint (see V1 migration) together guard against duplicate
 * entries if Kafka redelivers the event.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordTravelHistoryUseCase {

    private final TravelHistoryRepository repository;

    @Transactional
    public void execute(String userId, String bookingId, String resourceType,
                        String resourceName) {
        UserId id = UserId.of(userId);

        if (repository.existsByUserIdAndBookingId(id, bookingId)) {
            log.debug("Travel history entry already recorded for booking {} — skipping", bookingId);
            return;
        }

        TravelHistoryEntry entry = TravelHistoryEntry.of(
            userId, bookingId, resourceType, resourceName, Instant.now());

        repository.save(entry);
        log.info("Travel history recorded: user={} booking={}", userId, bookingId);
    }
}
