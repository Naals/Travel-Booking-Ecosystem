package com.travel.search.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;

/**
 * Latitude/longitude pair used for "near me" search.
 *
 * Named GeoCoordinates (not GeoPoint) deliberately — Spring Data
 * Elasticsearch has its own org.springframework.data.elasticsearch.core.geo.GeoPoint
 * class used for @GeoPointField mapping. Keeping domain and
 * infrastructure types distinctly named avoids import collisions
 * in the mapper and keeps the domain layer free of ES-specific types.
 */
public final class GeoCoordinates implements ValueObject {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final double latitude;
    private final double longitude;

    private GeoCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90)
            throw new DomainException("Latitude must be between -90 and 90", "INVALID_COORDINATES");
        if (longitude < -180 || longitude > 180)
            throw new DomainException("Longitude must be between -180 and 180", "INVALID_COORDINATES");
        this.latitude  = latitude;
        this.longitude = longitude;
    }

    public static GeoCoordinates of(double latitude, double longitude) {
        return new GeoCoordinates(latitude, longitude);
    }

    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }

    /**
     * Great-circle distance to another point, in kilometers (Haversine formula).
     * Used for validating geo-search results and could back a client-side
     * "X km away" display.
     */
    public double distanceKmTo(GeoCoordinates other) {
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(this.latitude))
            * Math.cos(Math.toRadians(other.latitude))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override public boolean equals(Object o) {
        return o instanceof GeoCoordinates g
            && Double.compare(latitude, g.latitude) == 0
            && Double.compare(longitude, g.longitude) == 0;
    }
    @Override public int    hashCode() { return Objects.hash(latitude, longitude); }
    @Override public String toString() { return latitude + "," + longitude; }
}
