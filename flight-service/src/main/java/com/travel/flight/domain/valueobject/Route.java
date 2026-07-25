package com.travel.flight.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Flight route value object.
 * Encapsulates origin, destination, and departure/arrival schedule.
 * All times are stored with timezone offset (ZonedDateTime) — critical
 * for international flights where local times differ.
 *
 * IATA airport codes are 3-letter uppercase (e.g. "IST", "JFK", "CDG").
 */
public final class Route implements ValueObject {

    private final String        originCode;
    private final String        destinationCode;
    private final String        originCity;
    private final String        destinationCity;
    private final ZonedDateTime departureTime;
    private final ZonedDateTime arrivalTime;

    private Route(String originCode, String destinationCode,
                  String originCity, String destinationCity,
                  ZonedDateTime departureTime, ZonedDateTime arrivalTime) {
        if (originCode == null || originCode.length() != 3)
            throw new DomainException("Origin IATA code must be 3 characters", "INVALID_ROUTE");
        if (destinationCode == null || destinationCode.length() != 3)
            throw new DomainException("Destination IATA code must be 3 characters", "INVALID_ROUTE");
        if (originCode.equalsIgnoreCase(destinationCode))
            throw new DomainException("Origin and destination must be different", "INVALID_ROUTE");
        if (departureTime == null || arrivalTime == null)
            throw new DomainException("Departure and arrival times are required", "INVALID_ROUTE");
        if (!departureTime.isBefore(arrivalTime))
            throw new DomainException("Departure must be before arrival", "INVALID_ROUTE");

        this.originCode      = originCode.toUpperCase();
        this.destinationCode = destinationCode.toUpperCase();
        this.originCity      = Objects.requireNonNull(originCity);
        this.destinationCity = Objects.requireNonNull(destinationCity);
        this.departureTime   = departureTime;
        this.arrivalTime     = arrivalTime;
    }

    public static Route of(String originCode, String destinationCode,
                           String originCity, String destinationCity,
                           ZonedDateTime departureTime, ZonedDateTime arrivalTime) {
        return new Route(originCode, destinationCode, originCity, destinationCity,
            departureTime, arrivalTime);
    }

    /**
     * Flight duration calculated from departure to arrival.
     * Accounts for timezone differences automatically via ZonedDateTime.
     */
    public Duration flightDuration() {
        return Duration.between(departureTime, arrivalTime);
    }

    public String        getOriginCode()      { return originCode; }
    public String        getDestinationCode() { return destinationCode; }
    public String        getOriginCity()      { return originCity; }
    public String        getDestinationCity() { return destinationCity; }
    public ZonedDateTime getDepartureTime()   { return departureTime; }
    public ZonedDateTime getArrivalTime()     { return arrivalTime; }

    public String toDisplayString() {
        return originCode + " → " + destinationCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Route r)) return false;
        return Objects.equals(originCode, r.originCode)
            && Objects.equals(destinationCode, r.destinationCode)
            && Objects.equals(departureTime, r.departureTime);
    }
    @Override public int    hashCode() { return Objects.hash(originCode, destinationCode, departureTime); }
    @Override public String toString() { return toDisplayString() + " @ " + departureTime; }
}
