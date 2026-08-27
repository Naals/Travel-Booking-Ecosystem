package com.travel.analytics.domain.model;

/**
 * Mirrors booking-service's BookingType (Day 7) but is this service's
 * own enum — bounded contexts don't share Java types across service
 * boundaries in this platform, only string values over Kafka. Same
 * convention ReviewedResourceType (review-service, Day 16) followed.
 */
public enum BookingType {
    PROPERTY,
    HOTEL,
    FLIGHT,
    VEHICLE
}
