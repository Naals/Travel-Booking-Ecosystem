package com.travel.vehicle.infrastructure.persistence.mapper;

import com.travel.vehicle.domain.aggregate.Vehicle;
import com.travel.vehicle.domain.model.VehicleRental;
import com.travel.vehicle.domain.valueobject.*;
import com.travel.vehicle.infrastructure.persistence.entity.VehicleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleJpaEntity toEntity(Vehicle v) {
        VehicleJpaEntity e = VehicleJpaEntity.builder()
            .id(v.getId().getValue())
            .make(v.getSpec().getMake())
            .model(v.getSpec().getModel())
            .year(v.getSpec().getYear())
            .licensePlate(v.getSpec().getLicensePlate())
            .seats(v.getSpec().getSeats())
            .transmission(v.getSpec().getTransmission())
            .fuelType(v.getSpec().getFuelType())
            .airConditioning(v.getSpec().hasAirConditioning())
            .category(v.getCategory())
            .status(v.getStatus())
            .homeLocationCode(v.getHomeLocation().getLocationCode())
            .homeLocationCity(v.getHomeLocation().getCity())
            .homeLocationCountry(v.getHomeLocation().getCountry())
            .homeLocationAddress(v.getHomeLocation().getAddress())
            .currentLocationCode(v.getCurrentLocation().getLocationCode())
            .currentLocationCity(v.getCurrentLocation().getCity())
            .currentLocationCountry(v.getCurrentLocation().getCountry())
            .dailyRate(v.getDailyRate().getAmount())
            .currency(v.getDailyRate().getCurrency())
            .createdAt(v.getCreatedAt())
            .updatedAt(v.getUpdatedAt())
            .build();

        // Persist active rental state
        v.getActiveRental().ifPresent(r -> {
            e.setRentalBookingId(r.getBookingId());
            e.setRentalUserId(r.getUserId());
            e.setRentalPickupDate(r.getRentalPeriod().getStart());
            e.setRentalReturnDate(r.getRentalPeriod().getEnd());
            e.setRentalPickupLocationCode(r.getPickupLocation().getLocationCode());
            e.setRentalPickupLocationCity(r.getPickupLocation().getCity());
            e.setRentalReturnLocationCode(r.getReturnLocation().getLocationCode());
            e.setRentalReturnLocationCity(r.getReturnLocation().getCity());
            e.setRentalConfirmed(r.isConfirmed());
        });

        return e;
    }

    public Vehicle toDomain(VehicleJpaEntity e) {
        VehicleSpec spec = VehicleSpec.of(
            e.getMake(), e.getModel(), e.getYear(),
            e.getLicensePlate(), e.getSeats(),
            e.getTransmission(), e.getFuelType(),
            e.isAirConditioning());

        PickupLocation home = PickupLocation.of(
            e.getHomeLocationCode(), e.getHomeLocationCity(),
            e.getHomeLocationCountry(), e.getHomeLocationAddress());

        PickupLocation current = PickupLocation.of(
            e.getCurrentLocationCode(), e.getCurrentLocationCity(),
            e.getCurrentLocationCountry(), "");

        VehicleRental rental = null;
        if (e.getRentalBookingId() != null) {
            PickupLocation pickupLoc = PickupLocation.of(
                e.getRentalPickupLocationCode(),
                e.getRentalPickupLocationCity(), "", "");
            PickupLocation returnLoc = PickupLocation.of(
                e.getRentalReturnLocationCode(),
                e.getRentalReturnLocationCity(), "", "");
            rental = new VehicleRental(
                e.getRentalBookingId(), e.getRentalUserId(),
                DateRange.of(e.getRentalPickupDate(), e.getRentalReturnDate()),
                pickupLoc, returnLoc);
            if (Boolean.TRUE.equals(e.getRentalConfirmed())) rental.confirm();
        }

        return Vehicle.reconstitute(
            VehicleId.of(e.getId()),
            spec,
            e.getCategory(),
            e.getStatus(),
            home,
            current,
            Money.of(e.getDailyRate(), e.getCurrency()),
            rental,
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
