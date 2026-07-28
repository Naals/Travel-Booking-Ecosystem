package com.travel.vehicle.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * Vehicle specification — make, model, year, and technical details.
 * Immutable description of the physical car.
 */
public final class VehicleSpec implements ValueObject {

    private final String           make;
    private final String           model;
    private final int              year;
    private final String           licensePlate;
    private final int              seats;
    private final TransmissionType transmission;
    private final FuelType         fuelType;
    private final boolean          airConditioning;

    private VehicleSpec(String make, String model, int year, String licensePlate,
                        int seats, TransmissionType transmission,
                        FuelType fuelType, boolean airConditioning) {
        if (make == null || make.isBlank())
            throw new DomainException("Make is required", "INVALID_SPEC");
        if (model == null || model.isBlank())
            throw new DomainException("Model is required", "INVALID_SPEC");
        if (year < 1990 || year > 2100)
            throw new DomainException("Invalid vehicle year", "INVALID_SPEC");
        if (seats < 1 || seats > 20)
            throw new DomainException("Seat count must be between 1 and 20", "INVALID_SPEC");
        if (licensePlate == null || licensePlate.isBlank())
            throw new DomainException("License plate is required", "INVALID_SPEC");
        this.make            = make.trim();
        this.model           = model.trim();
        this.year            = year;
        this.licensePlate    = licensePlate.trim().toUpperCase();
        this.seats           = seats;
        this.transmission    = Objects.requireNonNull(transmission);
        this.fuelType        = Objects.requireNonNull(fuelType);
        this.airConditioning = airConditioning;
    }

    public static VehicleSpec of(String make, String model, int year,
                                 String licensePlate, int seats,
                                 TransmissionType transmission,
                                 FuelType fuelType, boolean airConditioning) {
        return new VehicleSpec(make, model, year, licensePlate,
            seats, transmission, fuelType, airConditioning);
    }

    public String           getMake()            { return make; }
    public String           getModel()           { return model; }
    public int              getYear()            { return year; }
    public String           getLicensePlate()    { return licensePlate; }
    public int              getSeats()           { return seats; }
    public TransmissionType getTransmission()    { return transmission; }
    public FuelType         getFuelType()        { return fuelType; }
    public boolean          hasAirConditioning() { return airConditioning; }

    public String toDisplayString() {
        return year + " " + make + " " + model;
    }

    @Override public boolean equals(Object o) {
        return o instanceof VehicleSpec s
            && Objects.equals(licensePlate, s.licensePlate);
    }
    @Override public int    hashCode() { return Objects.hash(licensePlate); }
    @Override public String toString() { return toDisplayString() + " [" + licensePlate + "]"; }
}
