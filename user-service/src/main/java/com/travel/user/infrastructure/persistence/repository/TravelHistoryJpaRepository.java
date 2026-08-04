package com.travel.user.infrastructure.persistence.repository;

import com.travel.user.infrastructure.persistence.entity.TravelHistoryEntryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelHistoryJpaRepository extends JpaRepository<TravelHistoryEntryJpaEntity, Long> {
    boolean existsByUserIdAndBookingId(String userId, String bookingId);
    Page<TravelHistoryEntryJpaEntity> findByUserId(String userId, Pageable pageable);
    long    countByUserId(String userId);
}
