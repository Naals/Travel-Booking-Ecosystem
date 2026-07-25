package com.travel.flight.domain.event;

import com.travel.shared.event.DomainEvent;
import java.time.ZonedDateTime;

/**
 * Published when a flight is scheduled and made available for booking.
 * Consumed by search-service to index the flight in Elasticsearch.
 */
public class FlightScheduledEvent extends DomainEvent {

    private final String        flightId;
    private final String        flightNumber;
    private final String        originCode;
    private final String        destinationCode;
    private final ZonedDateTime departureTime;
    private final ZonedDateTime arrivalTime;

    public FlightScheduledEvent(String flightId, String flightNumber,
                                String originCode, String destinationCode,
                                ZonedDateTime departureTime, ZonedDateTime arrivalTime) {
        super("FlightScheduled");
        this.flightId        = flightId;
        this.flightNumber    = flightNumber;
        this.originCode      = originCode;
        this.destinationCode = destinationCode;
        this.departureTime   = departureTime;
        this.arrivalTime     = arrivalTime;
    }

    @Override public String getAggregateId()    { return flightId; }
    public String        getFlightId()           { return flightId; }
    public String        getFlightNumber()       { return flightNumber; }
    public String        getOriginCode()         { return originCode; }
    public String        getDestinationCode()    { return destinationCode; }
    public ZonedDateTime getDepartureTime()      { return departureTime; }
    public ZonedDateTime getArrivalTime()        { return arrivalTime; }
}
