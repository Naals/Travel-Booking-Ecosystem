package com.travel.analytics.infrastructure.persistence.repository;

import com.travel.analytics.infrastructure.persistence.entity.BookingTypeLookupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingTypeLookupJpaRepository extends JpaRepository<BookingTypeLookupJpaEntity, String> {
}
