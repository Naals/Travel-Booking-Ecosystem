package com.travel.recommendation.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * A (city, country) pair used as the correlation key across
 * DestinationLookup, UserAffinity, and DestinationPopularity.
 *
 * country is normalized to uppercase, matching every Address value
 * object elsewhere in the platform (property-service Day 10,
 * hotel-service Day 11, vehicle-service Day 13). city is only
 * trimmed, NOT case-folded — a known, accepted limitation: "istanbul"
 * and "Istanbul" from two different upstream events would be treated
 * as different destinations. Fixing this would need a canonicalization
 * step (geocoding, a reference city table) this service doesn't have;
 * see ADR-012.
 */
public final class DestinationKey implements ValueObject {

    private final String city;
    private final String country;

    private DestinationKey(String city, String country) {
        if (city == null || city.isBlank())
            throw new DomainException("City must not be empty", "INVALID_DESTINATION_KEY");
        if (country == null || country.isBlank())
            throw new DomainException("Country must not be empty", "INVALID_DESTINATION_KEY");
        this.city    = city.trim();
        this.country = country.trim().toUpperCase();
    }

    public static DestinationKey of(String city, String country) {
        return new DestinationKey(city, country);
    }

    public String getCity()    { return city; }
    public String getCountry() { return country; }

    @Override public boolean equals(Object o) {
        return o instanceof DestinationKey d
            && city.equals(d.city) && country.equals(d.country);
    }
    @Override public int    hashCode() { return Objects.hash(city, country); }
    @Override public String toString() { return city + ", " + country; }
}
