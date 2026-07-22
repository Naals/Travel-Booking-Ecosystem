package com.travel.property.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a property's availability changes.
 * Consumed by search-service to update the search index.
 */
public class AvailabilityUpdatedEvent extends DomainEvent {

    private final String propertyId;

    public AvailabilityUpdatedEvent(String propertyId) {
        super("AvailabilityUpdated");
        this.propertyId = propertyId;
    }

    @Override public String getAggregateId() { return propertyId; }
    public String getPropertyId() { return propertyId; }
}
