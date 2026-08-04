package com.travel.user.domain.repository;

import com.travel.user.domain.model.TravelHistoryEntry;
import com.travel.user.domain.model.UserId;

import java.util.List;

/**
 * Separate port from UserProfileRepository — travel history is a CQRS
 * projection (see TravelHistoryEntry Javadoc), not part of the
 * UserProfile aggregate, so it is persisted and queried independently.
 */
public interface TravelHistoryRepository {
    void    save(TravelHistoryEntry entry);
    boolean existsByUserIdAndBookingId(UserId userId, String bookingId);
    List<TravelHistoryEntry> findByUserId(UserId userId, int page, int size);
    long    countByUserId(UserId userId);
}
