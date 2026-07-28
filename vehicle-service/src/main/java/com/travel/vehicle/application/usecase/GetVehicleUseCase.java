package com.travel.vehicle.application.usecase;

import com.travel.vehicle.application.dto.response.VehicleResponse;
import com.travel.vehicle.domain.repository.VehicleRepository;
import com.travel.vehicle.domain.service.FleetQueryService;
import com.travel.vehicle.domain.valueobject.VehicleId;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetVehicleUseCase {

    private final VehicleRepository  repository;
    private final FleetQueryService  fleetQueryService;

    @Transactional(readOnly = true)
    public VehicleResponse execute(String vehicleId) {
        return repository.findById(VehicleId.of(vehicleId))
            .map(VehicleResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> executeAvailable(String locationCode,
                                                  LocalDate pickupDate,
                                                  LocalDate returnDate) {
        return fleetQueryService.findAllAvailable(locationCode, pickupDate, returnDate)
            .stream()
            .map(VehicleResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> executeByLocation(String locationCode) {
        return repository.findByLocationCode(locationCode)
            .stream()
            .map(VehicleResponse::from)
            .toList();
    }
}
