package com.travel.vehicle.domain.valueobject;

/**
 * Vehicle category determines pricing tier, capacity, and fuel type defaults.
 * Customers book by category — not by specific car.
 * "Or similar" guarantee: booked category = minimum category delivered.
 */
public enum VehicleCategory {
    ECONOMY,
    COMPACT,
    MIDSIZE,
    FULL_SIZE,
    SUV,
    LUXURY,
    VAN,
    PICKUP_TRUCK,
    ELECTRIC,
    CONVERTIBLE
}
