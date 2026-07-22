package com.travel.property.domain.valueobject;

/**
 * Standard amenity list. Stored as a Set<Amenity> on the Property aggregate.
 * Adding new amenities is a backward-compatible change — existing properties
 * are unaffected.
 */
public enum Amenity {
    WIFI,
    AIR_CONDITIONING,
    HEATING,
    KITCHEN,
    WASHING_MACHINE,
    DRYER,
    PARKING,
    POOL,
    GYM,
    PET_FRIENDLY,
    SMOKING_ALLOWED,
    WHEELCHAIR_ACCESSIBLE,
    BALCONY,
    GARDEN,
    BBQ_GRILL,
    FIREPLACE,
    HOT_TUB,
    TV,
    WORKSPACE,
    ELEVATOR
}
