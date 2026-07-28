package com.travel.vehicle.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * Physical location where a vehicle can be picked up or returned.
 * Supports airport codes (IATA) and city branch identifiers.
 * One-way rentals have different pickupLocation and returnLocation.
 */
public final class PickupLocation implements ValueObject {

    private final String locationCode; // branch identifier or IATA airport code
    private final String city;
    private final String country;
    private final String address;

    private PickupLocation(String locationCode, String city,
                           String country, String address) {
        if (locationCode == null || locationCode.isBlank())
            throw new DomainException("Location code is required", "INVALID_LOCATION");
        if (city == null || city.isBlank())
            throw new DomainException("City is required", "INVALID_LOCATION");
        this.locationCode = locationCode.toUpperCase().trim();
        this.city         = city.trim();
        this.country      = country != null ? country.trim().toUpperCase() : "";
        this.address      = address != null ? address.trim() : "";
    }

    public static PickupLocation of(String locationCode, String city,
                                    String country, String address) {
        return new PickupLocation(locationCode, city, country, address);
    }

    public String getLocationCode() { return locationCode; }
    public String getCity()         { return city; }
    public String getCountry()      { return country; }
    public String getAddress()      { return address; }

    public boolean isSameLocation(PickupLocation other) {
        return locationCode.equals(other.locationCode);
    }

    @Override public boolean equals(Object o) {
        return o instanceof PickupLocation p
            && Objects.equals(locationCode, p.locationCode);
    }
    @Override public int    hashCode() { return Objects.hash(locationCode); }
    @Override public String toString() { return locationCode + " (" + city + ")"; }
}
