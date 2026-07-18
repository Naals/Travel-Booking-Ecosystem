package com.travel.booking.domain.repository;

import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.valueobject.BookingId;
import com.travel.booking.domain.valueobject.BookingStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository {
    Booking        save(Booking booking);
    Optional<Booking> findById(BookingId id);
    List<Booking>  findByUserId(String userId);
    List<Booking>  findByStatus(BookingStatus status);
    List<Booking>  findByUserIdAndStatus(String userId, BookingStatus status);
}
