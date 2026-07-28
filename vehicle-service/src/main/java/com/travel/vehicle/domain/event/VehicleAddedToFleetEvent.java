package com.travel.vehicle.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a vehicle is added to the active fleet.
 * Consumed by search-service to index rental options.
 */
public class VehicleAddedToFleetEvent extends DomainEvent {

    private final String vehicleId;
    private final String category;
    private final String locationCode;
    private final String city;
    private final String country;

    public VehicleAddedToFleetEvent(String vehicleId, String category,
                                    String locationCode, String city,
                                    String country) {
        super("VehicleAddedToFleet");
        this.vehicleId    = vehicleId;
        this.category     = category;
        this.locationCode = locationCode;
        this.city         = city;
        this.country      = country;
    }

    @Override public String getAggregateId() { return vehicleId; }
    public String getVehicleId()    { return vehicleId; }
    public String getCategory()     { return category; }
    public String getLocationCode() { return locationCode; }
    public String getCity()         { return city; }
    public String getCountry()      { return country; }
}
