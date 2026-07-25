package com.travel.flight.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when flight status changes (e.g. SCHEDULED → DELAYED).
 * Consumed by notification-service to alert affected passengers.
 */
public class FlightStatusChangedEvent extends DomainEvent {

    private final String flightId;
    private final String flightNumber;
    private final String previousStatus;
    private final String newStatus;
    private final String reason;

    public FlightStatusChangedEvent(String flightId, String flightNumber,
                                    String previousStatus, String newStatus,
                                    String reason) {
        super("FlightStatusChanged");
        this.flightId       = flightId;
        this.flightNumber   = flightNumber;
        this.previousStatus = previousStatus;
        this.newStatus      = newStatus;
        this.reason         = reason;
    }

    @Override public String getAggregateId()  { return flightId; }
    public String getFlightId()               { return flightId; }
    public String getFlightNumber()           { return flightNumber; }
    public String getPreviousStatus()         { return previousStatus; }
    public String getNewStatus()              { return newStatus; }
    public String getReason()                 { return reason; }
}
