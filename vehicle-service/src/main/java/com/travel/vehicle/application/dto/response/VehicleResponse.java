package com.travel.vehicle.application.dto.response;

import com.travel.vehicle.domain.aggregate.Vehicle;
import java.math.BigDecimal;
import java.time.Instant;

public record VehicleResponse(
    String     vehicleId,
    String     make,
    String     model,
    int        year,
    String     licensePlate,
    int        seats,
    String     transmission,
    String     fuelType,
    boolean    airConditioning,
    String     category,
    String     status,
    String     locationCode,
    String     locationCity,
    String     locationCountry,
    BigDecimal dailyRate,
    String     currency,
    Instant    createdAt
) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(
            v.getId().getValue(),
            v.getSpec().getMake(),
            v.getSpec().getModel(),
            v.getSpec().getYear(),
            v.getSpec().getLicensePlate(),
            v.getSpec().getSeats(),
            v.getSpec().getTransmission().name(),
            v.getSpec().getFuelType().name(),
            v.getSpec().hasAirConditioning(),
            v.getCategory().name(),
            v.getStatus().name(),
            v.getCurrentLocation().getLocationCode(),
            v.getCurrentLocation().getCity(),
            v.getCurrentLocation().getCountry(),
            v.getDailyRate().getAmount(),
            v.getDailyRate().getCurrency(),
            v.getCreatedAt()
        );
    }
}
