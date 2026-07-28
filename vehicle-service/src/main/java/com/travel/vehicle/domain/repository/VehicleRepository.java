package com.travel.vehicle.domain.repository;

import com.travel.vehicle.domain.aggregate.Vehicle;
import com.travel.vehicle.domain.valueobject.VehicleCategory;
import com.travel.vehicle.domain.valueobject.VehicleId;
import com.travel.vehicle.domain.valueobject.VehicleStatus;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    Vehicle           save(Vehicle vehicle);
    Optional<Vehicle> findById(VehicleId id);
    List<Vehicle>     findByStatus(VehicleStatus status);
    List<Vehicle>     findByCategoryAndLocationCode(
        VehicleCategory category, String locationCode);
    List<Vehicle>     findByLocationCode(String locationCode);
    boolean           existsById(VehicleId id);
}
