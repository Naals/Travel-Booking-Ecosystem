package com.travel.vehicle.domain.model;

import com.travel.vehicle.domain.valueobject.DateRange;
import com.travel.vehicle.domain.valueobject.PickupLocation;
import java.util.Objects;

/**
 * A rental hold on a specific vehicle.
 * Tracks pickup/return locations to support one-way rentals.
 * Owned by the Vehicle aggregate — not an independent aggregate.
 */
public final class VehicleRental {

    private final String         bookingId;
    private final String         userId;
    private final DateRange      rentalPeriod;
    private final PickupLocation pickupLocation;
    private final PickupLocation returnLocation;
    private       boolean        confirmed;

    public VehicleRental(String bookingId, String userId,
                         DateRange rentalPeriod,
                         PickupLocation pickupLocation,
                         PickupLocation returnLocation) {
        this.bookingId      = Objects.requireNonNull(bookingId);
        this.userId         = Objects.requireNonNull(userId);
        this.rentalPeriod   = Objects.requireNonNull(rentalPeriod);
        this.pickupLocation = Objects.requireNonNull(pickupLocation);
        this.returnLocation = Objects.requireNonNull(returnLocation);
        this.confirmed      = false;
    }

    public void confirm()  { this.confirmed = true; }

    public boolean isOneWay() {
        return !pickupLocation.isSameLocation(returnLocation);
    }

    public String         getBookingId()      { return bookingId; }
    public String         getUserId()         { return userId; }
    public DateRange      getRentalPeriod()   { return rentalPeriod; }
    public PickupLocation getPickupLocation() { return pickupLocation; }
    public PickupLocation getReturnLocation() { return returnLocation; }
    public boolean        isConfirmed()       { return confirmed; }
}
