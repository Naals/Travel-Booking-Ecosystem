package com.travel.analytics.domain.repository;

import com.travel.analytics.domain.model.BookingType;
import java.util.Optional;

/**
 * Correlation cache: bookingId → the type it was created as.
 *
 * BookingConfirmedEvent and BookingCancelledEvent (booking-service,
 * Day 7) carry no bookingType field — only BookingCreatedEvent (Day 7)
 * and BookingCompletedEvent (enriched Day 15) do. This is the third
 * instance of the "local lookup bridging two events" shape in this
 * platform, after SpendRecord (loyalty-service, Day 19) and
 * DestinationLookup (recommendation-service, Day 20) — a recognized,
 * deliberate pattern here, not an ad-hoc workaround invented fresh
 * each time.
 */
public interface BookingTypeLookupRepository {
    void                   record(String bookingId, BookingType type);
    Optional<BookingType>  findByBookingId(String bookingId);
}
