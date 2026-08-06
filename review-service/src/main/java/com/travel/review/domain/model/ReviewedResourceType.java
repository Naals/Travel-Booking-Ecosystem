package com.travel.review.domain.model;

/**
 * Mirrors booking-service's BookingType (Day 7) but is this service's
 * own enum — bounded contexts don't share Java types across service
 * boundaries in this platform, only string values over Kafka.
 */
public enum ReviewedResourceType {
    PROPERTY,
    HOTEL,
    FLIGHT,
    VEHICLE
}
