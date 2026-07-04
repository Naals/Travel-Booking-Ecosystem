package com.travel.shared.domain;

/**
 * Marker interface for DDD Value Objects.
 *
 * Value objects are:
 * - Immutable — no setters, all state set at construction
 * - Equality by attributes — not by reference or identity
 * - Self-validating — constructor rejects invalid state
 *
 * Examples in this platform: Email, Money, BookingId, FullName, PhoneNumber.
 *
 * Implementations must override equals() and hashCode() based on all fields.
 */
public interface ValueObject {
}
