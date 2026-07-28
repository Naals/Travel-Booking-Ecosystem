package com.travel.vehicle.domain.valueobject;

public enum VehicleStatus {
    AVAILABLE,       // ready for rental
    RESERVED,        // hold placed — pending payment
    RENTED,          // actively rented out (booking confirmed)
    MAINTENANCE,     // in service — not rentable
    DECOMMISSIONED   // permanently removed from fleet
}
