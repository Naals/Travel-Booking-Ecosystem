package com.travel.booking.domain.valueobject;

/**
 * Booking lifecycle states — mirrors the saga state machine exactly.
 *
 * Happy path:
 *   INITIATED → INVENTORY_RESERVED → PAYMENT_PENDING → CONFIRMED → COMPLETED
 *
 * Compensation paths:
 *   INITIATED        → INVENTORY_FAILED → CANCELLED
 *   PAYMENT_PENDING  → PAYMENT_FAILED   → INVENTORY_RELEASING → CANCELLED
 */
public enum BookingStatus {
    INITIATED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    PAYMENT_PENDING,
    PAYMENT_FAILED,
    INVENTORY_RELEASING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}
