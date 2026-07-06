package com.travel.common.exception;

/**
 * Thrown when a requested aggregate or entity does not exist.
 * Maps to HTTP 404 in GlobalExceptionHandler.
 *
 * Usage:
 *   throw new ResourceNotFoundException("Booking", bookingId);
 *   throw new ResourceNotFoundException("User not found");
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceType, String id) {
        super(resourceType + " not found with id: " + id, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}
