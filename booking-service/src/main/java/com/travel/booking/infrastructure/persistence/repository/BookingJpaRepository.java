package com.travel.booking.infrastructure.persistence.repository;

import com.travel.booking.domain.valueobject.BookingStatus;
import com.travel.booking.infrastructure.persistence.entity.BookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingJpaEntity, String> {
    List<BookingJpaEntity> findByUserId(String userId);
    List<BookingJpaEntity> findByStatus(BookingStatus status);
    List<BookingJpaEntity> findByUserIdAndStatus(String userId, BookingStatus status);
}
