package com.travel.search.domain.model;

/**
 * Discriminator for the unified "listings" index.
 * Every inventory service (property, hotel, flight, vehicle) indexes
 * into the same Elasticsearch index — this field is how a query can
 * scope to one type or search across all of them at once.
 */
public enum ListingType {
    PROPERTY,
    HOTEL,
    FLIGHT,
    VEHICLE
}
