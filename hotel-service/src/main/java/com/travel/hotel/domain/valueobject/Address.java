package com.travel.hotel.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

public final class Address implements ValueObject {

    private final String street;
    private final String city;
    private final String country;
    private final double latitude;
    private final double longitude;

    private Address(String street, String city, String country,
                    double latitude, double longitude) {
        if (street  == null || street.isBlank())  throw new DomainException("Street required",  "INVALID_ADDRESS");
        if (city    == null || city.isBlank())     throw new DomainException("City required",    "INVALID_ADDRESS");
        if (country == null || country.isBlank())  throw new DomainException("Country required", "INVALID_ADDRESS");
        this.street    = street.trim();
        this.city      = city.trim();
        this.country   = country.trim().toUpperCase();
        this.latitude  = latitude;
        this.longitude = longitude;
    }

    public static Address of(String street, String city, String country,
                             double latitude, double longitude) {
        return new Address(street, city, country, latitude, longitude);
    }

    public String getStreet()    { return street; }
    public String getCity()      { return city; }
    public String getCountry()   { return country; }
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }

    @Override public boolean equals(Object o) {
        return o instanceof Address a
            && Objects.equals(street, a.street)
            && Objects.equals(city, a.city)
            && Objects.equals(country, a.country);
    }
    @Override public int hashCode() { return Objects.hash(street, city, country); }
}
