package com.travel.vehicle.application.usecase;

import com.travel.vehicle.application.dto.request.AddVehicleRequest;
import com.travel.vehicle.application.dto.response.VehicleResponse;
import com.travel.vehicle.domain.aggregate.Vehicle;
import com.travel.vehicle.domain.repository.VehicleRepository;
import com.travel.vehicle.domain.valueobject.*;
import com.travel.vehicle.infrastructure.messaging.producer.VehicleEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddVehicleToFleetUseCase {

    private final VehicleRepository    repository;
    private final VehicleEventPublisher eventPublisher;

    @Transactional
    public VehicleResponse execute(AddVehicleRequest request) {
        log.info("Adding vehicle to fleet: {} {} {}",
            request.year(), request.make(), request.model());

        VehicleSpec spec = VehicleSpec.of(
            request.make(), request.model(), request.year(),
            request.licensePlate(), request.seats(),
            TransmissionType.valueOf(request.transmissionType()),
            FuelType.valueOf(request.fuelType()),
            request.airConditioning());

        PickupLocation location = PickupLocation.of(
            request.locationCode(), request.locationCity(),
            request.locationCountry(), request.locationAddress());

        Money dailyRate = Money.of(request.dailyRate(), request.currency());

        Vehicle vehicle = Vehicle.addToFleet(
            spec,
            VehicleCategory.valueOf(request.category()),
            location,
            dailyRate);

        Vehicle saved = repository.save(vehicle);

        // VehicleAddedToFleetEvent → search-service indexes this vehicle
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Vehicle added to fleet: {}", saved.getId().getValue());
        return VehicleResponse.from(saved);
    }
}
