package com.travel.identity.domain.model;

/**
 * RBAC roles. Permissions are derived from roles in Spring Security config.
 * TRAVELER is the base role every user gets on registration.
 */
public enum Role {
    TRAVELER,       // can search and book
    HOST,           // can list properties
    HOTEL_MANAGER,  // can manage hotel inventory
    SUPPORT_AGENT,  // can view all bookings
    ADMIN           // full platform access
}
