package com.travel.booking.infrastructure.persistence;

import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.repository.BookingRepository;
import com.travel.booking.domain.valueobject.BookingId;
import com.travel.booking.domain.valueobject.BookingStatus;
import com.travel.booking.infrastructure.persistence.mapper.BookingMapper;
import com.travel.booking.infrastructure.persistence.repository.BookingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository jpa;
    private final BookingMapper        mapper;

    @Override public Booking           save(Booking b)              { return mapper.toDomain(jpa.save(mapper.toEntity(b))); }
    @Override public Optional<Booking> findById(BookingId id)       { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public List<Booking>     findByUserId(String uid)     { return jpa.findByUserId(uid).stream().map(mapper::toDomain).toList(); }
    @Override public List<Booking>     findByStatus(BookingStatus s){ return jpa.findByStatus(s).stream().map(mapper::toDomain).toList(); }
    @Override public List<Booking>     findByUserIdAndStatus(String uid, BookingStatus s) {
        return jpa.findByUserIdAndStatus(uid, s).stream().map(mapper::toDomain).toList();
    }
}
