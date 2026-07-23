package com.travel.property.infrastructure.persistence.repository;

import com.travel.property.domain.valueobject.PropertyStatus;
import com.travel.property.infrastructure.persistence.entity.PropertyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PropertyJpaRepository extends JpaRepository<PropertyJpaEntity, String> {
    List<PropertyJpaEntity> findByHostId(String hostId);
    List<PropertyJpaEntity> findByStatus(PropertyStatus status);
    List<PropertyJpaEntity> findByCityIgnoreCase(String city);
}
