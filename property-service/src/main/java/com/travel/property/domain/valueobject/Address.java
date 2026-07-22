package com.travel.property.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * Physical address value object.
 * Immutable. Equality by all fields.
 * country is ISO 3166-1 alpha-2 (e.g. "US", "TR", "DE").
 */
public final class Address implements ValueObject {

    private final String street;
    private final String city;
    private final String state;
    private final String country;
    private final String postalCode;
    private final double latitude;
    private final double longitude;

    private Address(String street, String city, String state,
                    String country, String postalCode,
                    double latitude, double longitude) {
        if (street  == null || street.isBlank())  throw new DomainException("Street is required",  "INVALID_ADDRESS");
        if (city    == null || city.isBlank())     throw new DomainException("City is required",    "INVALID_ADDRESS");
        if (country == null || country.isBlank())  throw new DomainException("Country is required", "INVALID_ADDRESS");
        this.street     = street.trim();
        this.city       = city.trim();
        this.state      = state != null ? state.trim() : "";
        this.country    = country.trim().toUpperCase();
        this.postalCode = postalCode != null ? postalCode.trim() : "";
        this.latitude   = latitude;
        this.longitude  = longitude;
    }

    public static Address of(String street, String city, String state,
                             String country, String postalCode,
                             double latitude, double longitude) {
        return new Address(street, city, state, country,
            postalCode, latitude, longitude);
    }

    public String getStreet()     { return street; }
    public String getCity()       { return city; }
    public String getState()      { return state; }
    public String getCountry()    { return country; }
    public String getPostalCode() { return postalCode; }
    public double getLatitude()   { return latitude; }
    public double getLongitude()  { return longitude; }

    public String toDisplayString() {
        return street + ", " + city + (state.isBlank() ? "" : ", " + state)
            + ", " + country;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Address a)) return false;
        return Objects.equals(street, a.street) && Objects.equals(city, a.city)
            && Objects.equals(country, a.country) && Objects.equals(postalCode, a.postalCode);
    }
    @Override public int hashCode() { return Objects.hash(street, city, country, postalCode); }
}
