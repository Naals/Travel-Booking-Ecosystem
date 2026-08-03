package com.travel.user.domain.model;

import com.travel.shared.domain.Entity;
import com.travel.common.exception.DomainException;

/**
 * A bookmarked location on a user's profile (e.g. "Home", "Dream trip
 * 2027"). Owned by the UserProfile aggregate — has identity so it can
 * be individually removed, but no independent lifecycle.
 *
 * latitude/longitude are optional: a saved location can be as coarse
 * as "Paris, FR" with no precise coordinates.
 */
public class SavedLocation extends Entity<SavedLocationId> {

    private final String label;
    private final String city;
    private final String country;
    private final Double latitude;
    private final Double longitude;

    public SavedLocation(SavedLocationId id, String label, String city,
                         String country, Double latitude, Double longitude) {
        super(id);
        if (label == null || label.isBlank())
            throw new DomainException("Saved location label must not be empty", "INVALID_SAVED_LOCATION");
        if (city == null || city.isBlank())
            throw new DomainException("Saved location city must not be empty", "INVALID_SAVED_LOCATION");
        if (latitude != null && (latitude < -90 || latitude > 90))
            throw new DomainException("Latitude must be between -90 and 90", "INVALID_SAVED_LOCATION");
        if (longitude != null && (longitude < -180 || longitude > 180))
            throw new DomainException("Longitude must be between -180 and 180", "INVALID_SAVED_LOCATION");

        this.label     = label.trim();
        this.city      = city.trim();
        this.country   = country != null ? country.trim().toUpperCase() : "";
        this.latitude  = latitude;
        this.longitude = longitude;
    }

    public String getLabel()     { return label; }
    public String getCity()      { return city; }
    public String getCountry()   { return country; }
    public Double getLatitude()  { return latitude; }
    public Double getLongitude() { return longitude; }
}
