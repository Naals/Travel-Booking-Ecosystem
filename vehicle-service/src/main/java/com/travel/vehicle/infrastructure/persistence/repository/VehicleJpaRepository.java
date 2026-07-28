package com.travel.vehicle.infrastructure.persistence.repository;

import com.travel.vehicle.domain.valueobject.VehicleCategory;
import com.travel.vehicle.domain.valueobject.VehicleStatus;
import com.travel.vehicle.infrastructure.persistence.entity.VehicleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, String> {

    List<VehicleJpaEntity> findByStatus(VehicleStatus status);

    List<VehicleJpaEntity> findByCategoryAndCurrentLocationCode(
        VehicleCategory category, String locationCode);

    List<VehicleJpaEntity> findByCurrentLocationCode(String locationCode);
}
