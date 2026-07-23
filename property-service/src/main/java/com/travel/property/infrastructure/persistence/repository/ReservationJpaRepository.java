package com.travel.property.infrastructure.persistence.repository;

import com.travel.property.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, Long> {
    List<ReservationJpaEntity> findByPropertyId(String propertyId);

    @Modifying
    @Query("DELETE FROM ReservationJpaEntity r WHERE r.propertyId = :propertyId AND r.bookingId = :bookingId")
    void deleteByPropertyIdAndBookingId(String propertyId, String bookingId);
}
