package com.travel.flight.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when seat availability changes.
 * Consumed by search-service to update Elasticsearch.
 */
public class SeatInventoryUpdatedEvent extends DomainEvent {

    private final String flightId;

    public SeatInventoryUpdatedEvent(String flightId) {
        super("SeatInventoryUpdated");
        this.flightId = flightId;
    }

    @Override public String getAggregateId() { return flightId; }
    public String getFlightId() { return flightId; }
}
