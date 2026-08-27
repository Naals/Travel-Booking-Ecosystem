package com.travel.analytics.infrastructure.persistence;

import com.travel.analytics.domain.model.BookingType;
import com.travel.analytics.domain.repository.BookingTypeLookupRepository;
import com.travel.analytics.infrastructure.persistence.entity.BookingTypeLookupJpaEntity;
import com.travel.analytics.infrastructure.persistence.repository.BookingTypeLookupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookingTypeLookupRepositoryAdapter implements BookingTypeLookupRepository {

    private final BookingTypeLookupJpaRepository jpa;

    @Override
    public void record(String bookingId, BookingType type) {
        jpa.save(BookingTypeLookupJpaEntity.builder().bookingId(bookingId).bookingType(type).build());
    }

    @Override
    public Optional<BookingType> findByBookingId(String bookingId) {
        return jpa.findById(bookingId).map(BookingTypeLookupJpaEntity::getBookingType);
    }
}
