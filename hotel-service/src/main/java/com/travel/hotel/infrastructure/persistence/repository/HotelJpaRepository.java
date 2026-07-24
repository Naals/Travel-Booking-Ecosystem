package com.travel.hotel.infrastructure.persistence.repository;

import com.travel.hotel.domain.valueobject.HotelStatus;
import com.travel.hotel.infrastructure.persistence.entity.HotelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HotelJpaRepository extends JpaRepository<HotelJpaEntity, String> {
    List<HotelJpaEntity> findByManagerId(String managerId);
    List<HotelJpaEntity> findByStatus(HotelStatus status);
    List<HotelJpaEntity> findByCityIgnoreCase(String city);
}
