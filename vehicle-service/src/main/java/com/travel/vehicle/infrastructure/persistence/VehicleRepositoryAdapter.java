package com.travel.vehicle.infrastructure.persistence;

import com.travel.vehicle.domain.aggregate.Vehicle;
import com.travel.vehicle.domain.repository.VehicleRepository;
import com.travel.vehicle.domain.valueobject.VehicleCategory;
import com.travel.vehicle.domain.valueobject.VehicleId;
import com.travel.vehicle.domain.valueobject.VehicleStatus;
import com.travel.vehicle.infrastructure.persistence.mapper.VehicleMapper;
import com.travel.vehicle.infrastructure.persistence.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VehicleRepositoryAdapter implements VehicleRepository {

    private final VehicleJpaRepository jpa;
    private final VehicleMapper        mapper;

    @Override public Vehicle           save(Vehicle v)              { return mapper.toDomain(jpa.save(mapper.toEntity(v))); }
    @Override public Optional<Vehicle> findById(VehicleId id)       { return jpa.findById(id.getValue()).map(mapper::toDomain); }
    @Override public List<Vehicle>     findByStatus(VehicleStatus s){ return jpa.findByStatus(s).stream().map(mapper::toDomain).toList(); }
    @Override public List<Vehicle>     findByCategoryAndLocationCode(VehicleCategory c, String loc) {
        return jpa.findByCategoryAndCurrentLocationCode(c, loc).stream().map(mapper::toDomain).toList();
    }
    @Override public List<Vehicle>     findByLocationCode(String loc) { return jpa.findByCurrentLocationCode(loc).stream().map(mapper::toDomain).toList(); }
    @Override public boolean           existsById(VehicleId id)      { return jpa.existsById(id.getValue()); }
}
