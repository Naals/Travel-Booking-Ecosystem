package com.travel.vehicle.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when vehicle availability changes.
 * Consumed by search-service to update Elasticsearch inventory data.
 */
public class VehicleAvailabilityUpdatedEvent extends DomainEvent {

    private final String vehicleId;
    private final String locationCode;

    public VehicleAvailabilityUpdatedEvent(String vehicleId, String locationCode) {
        super("VehicleAvailabilityUpdated");
        this.vehicleId    = vehicleId;
        this.locationCode = locationCode;
    }

    @Override public String getAggregateId() { return vehicleId; }
    public String getVehicleId()    { return vehicleId; }
    public String getLocationCode() { return locationCode; }
}
