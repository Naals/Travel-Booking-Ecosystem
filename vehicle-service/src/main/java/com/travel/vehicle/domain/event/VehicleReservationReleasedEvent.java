package com.travel.vehicle.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a vehicle reservation is released (compensation transaction).
 * Consumed by booking-service saga as inventory.reservation-released.
 */
public class VehicleReservationReleasedEvent extends DomainEvent {

    private final String vehicleId;
    private final String bookingId;
    private final String reason;

    public VehicleReservationReleasedEvent(String vehicleId,
                                           String bookingId, String reason) {
        super("VehicleReservationReleased");
        this.vehicleId = vehicleId;
        this.bookingId = bookingId;
        this.reason    = reason;
    }

    @Override public String getAggregateId() { return vehicleId; }
    public String getVehicleId() { return vehicleId; }
    public String getBookingId() { return bookingId; }
    public String getReason()    { return reason; }
}
