package com.travel.flight.domain.aggregate;

import com.travel.flight.domain.event.*;
import com.travel.flight.domain.model.Seat;
import com.travel.flight.domain.valueobject.*;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.*;

/**
 * Flight Aggregate Root.
 *
 * Owns a collection of Seat entities. Key invariants:
 * - A flight must be SCHEDULED to accept reservations
 * - Seat numbers must be unique within a flight
 * - CANCELLED flights cannot be booked
 *
 * Saga participation (FLIGHT booking type):
 *   BookingCreated →
 *     finds first available seat of requested class →
 *     seat.reserve() → raises SeatReservedEvent
 *     or publishes failure if no seat available
 *
 *   PaymentFailed →
 *     seat.releaseReservation() → SeatReservationReleasedEvent
 *
 *   BookingConfirmed →
 *     seat.confirmReservation() → OCCUPIED
 */
public class Flight extends AggregateRoot<FlightId> {

    private final String       airlineCode;
    private final String       flightNumber;
    private       Route        route;
    private       FlightStatus status;
    private final List<Seat>   seats;
    private       String       delayReason;
    private final Instant      createdAt;
    private       Instant      updatedAt;

    // ── Private constructor ───────────────────────────────────────────────────

    private Flight(FlightId id, String airlineCode, String flightNumber, Route route) {
        super(id);
        this.airlineCode  = airlineCode;
        this.flightNumber = flightNumber;
        this.route        = route;
        this.status       = FlightStatus.SCHEDULED;
        this.seats        = new ArrayList<>();
        this.createdAt    = Instant.now();
        this.updatedAt    = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Creates a new flight and makes it available for booking.
     * Raises FlightScheduledEvent for search-service indexing.
     */
    public static Flight schedule(String airlineCode, String flightNumber, Route route) {
        if (airlineCode == null || airlineCode.isBlank())
            throw new com.travel.common.exception.DomainException(
                "Airline code is required", "INVALID_FLIGHT");
        if (flightNumber == null || flightNumber.isBlank())
            throw new com.travel.common.exception.DomainException(
                "Flight number is required", "INVALID_FLIGHT");

        FlightId id     = FlightId.generate();
        Flight   flight = new Flight(id, airlineCode, flightNumber, route);

        flight.registerEvent(new FlightScheduledEvent(
            id.getValue(), flightNumber,
            route.getOriginCode(), route.getDestinationCode(),
            route.getDepartureTime(), route.getArrivalTime()));

        return flight;
    }

    public static Flight reconstitute(FlightId id, String airlineCode,
                                      String flightNumber, Route route,
                                      FlightStatus status, List<Seat> seats,
                                      String delayReason,
                                      Instant createdAt, Instant updatedAt) {
        Flight f = new Flight(id, airlineCode, flightNumber, route);
        f.status      = status;
        f.seats.addAll(seats != null ? seats : Collections.emptyList());
        f.delayReason = delayReason;
        return f;
    }

    // ── Seat management ───────────────────────────────────────────────────────

    /**
     * Adds a seat to the flight. Seat numbers must be unique.
     * Usually called in bulk when configuring a new aircraft.
     */
    public Seat addSeat(String seatNumber, SeatClass seatClass, Money price) {
        boolean duplicate = seats.stream()
            .anyMatch(s -> s.getSeatNumber().equals(seatNumber));
        if (duplicate)
            throw new BusinessRuleViolationException(
                "Seat number " + seatNumber + " already exists on flight " + flightNumber,
                "DUPLICATE_SEAT_NUMBER");

        Seat seat = new Seat(SeatId.generate(), getId().getValue(),
            seatNumber, seatClass, price);
        seats.add(seat);
        this.updatedAt = Instant.now();
        return seat;
    }

    /**
     * Bulk-loads seats when configuring standard aircraft layout.
     * Used by operators to seed a new flight's seat map.
     */
    public void loadSeats(List<SeatConfiguration> configs) {
        configs.forEach(c -> addSeat(c.seatNumber(), c.seatClass(), c.price()));
        registerEvent(new SeatInventoryUpdatedEvent(getId().getValue()));
    }

    // ── Saga participation ────────────────────────────────────────────────────

    /**
     * Reserves the first available seat of the requested class.
     * Raises SeatReservedEvent → booking-service saga advances.
     */
    public Seat reserveSeat(String bookingId, String userId, SeatClass seatClass) {
        assertBookable();

        Seat seat = seats.stream()
            .filter(s -> s.getSeatClass() == seatClass && s.isAvailable())
            .findFirst()
            .orElseThrow(() -> new BusinessRuleViolationException(
                "No " + seatClass.name() + " seats available on flight " + flightNumber,
                "NO_SEATS_AVAILABLE"));

        seat.reserve(bookingId, userId);
        this.updatedAt = Instant.now();

        registerEvent(new SeatReservedEvent(
            getId().getValue(), seat.getId().getValue(),
            seat.getSeatNumber(), seatClass.name(),
            bookingId, userId, seat.getPrice()));
        registerEvent(new SeatInventoryUpdatedEvent(getId().getValue()));

        return seat;
    }

    /**
     * Releases a seat reservation (compensation transaction).
     */
    public void releaseReservation(String bookingId, String reason) {
        seats.stream()
            .filter(s -> s.getReservation()
                .map(r -> r.getBookingId().equals(bookingId))
                .orElse(false))
            .findFirst()
            .ifPresentOrElse(
                seat -> {
                    seat.releaseReservation(bookingId);
                    this.updatedAt = Instant.now();
                    registerEvent(new SeatReservationReleasedEvent(
                        getId().getValue(), seat.getId().getValue(),
                        bookingId, reason));
                    registerEvent(new SeatInventoryUpdatedEvent(getId().getValue()));
                },
                () -> { throw new BusinessRuleViolationException(
                    "No seat reservation found for bookingId: " + bookingId,
                    "RESERVATION_NOT_FOUND"); });
    }

    /**
     * Confirms a seat reservation — seat becomes permanently OCCUPIED.
     */
    public void confirmReservation(String bookingId) {
        seats.stream()
            .filter(s -> s.getReservation()
                .map(r -> r.getBookingId().equals(bookingId))
                .orElse(false))
            .findFirst()
            .ifPresent(seat -> {
                seat.confirmReservation(bookingId);
                this.updatedAt = Instant.now();
            });
    }

    // ── Flight status management ──────────────────────────────────────────────

    public void delay(String reason) {
        if (status == FlightStatus.CANCELLED || status == FlightStatus.ARRIVED)
            throw new BusinessRuleViolationException(
                "Cannot delay a " + status + " flight", "INVALID_STATUS_TRANSITION");
        String previous  = this.status.name();
        this.status      = FlightStatus.DELAYED;
        this.delayReason = reason;
        this.updatedAt   = Instant.now();
        registerEvent(new FlightStatusChangedEvent(
            getId().getValue(), flightNumber, previous, "DELAYED", reason));
    }

    public void cancel(String reason) {
        if (status == FlightStatus.DEPARTED || status == FlightStatus.ARRIVED)
            throw new BusinessRuleViolationException(
                "Cannot cancel a flight that has already departed", "INVALID_STATUS_TRANSITION");
        String previous = this.status.name();
        this.status     = FlightStatus.CANCELLED;
        this.updatedAt  = Instant.now();
        registerEvent(new FlightStatusChangedEvent(
            getId().getValue(), flightNumber, previous, "CANCELLED", reason));
    }

    public void markBoarding() {
        if (status != FlightStatus.SCHEDULED && status != FlightStatus.DELAYED)
            throw new BusinessRuleViolationException(
                "Cannot board from status: " + status, "INVALID_STATUS_TRANSITION");
        String previous = this.status.name();
        this.status     = FlightStatus.BOARDING;
        this.updatedAt  = Instant.now();
        registerEvent(new FlightStatusChangedEvent(
            getId().getValue(), flightNumber, previous, "BOARDING", null));
    }

    public void markDeparted() {
        assertStatus(FlightStatus.BOARDING, "mark departed");
        String previous = this.status.name();
        this.status     = FlightStatus.DEPARTED;
        this.updatedAt  = Instant.now();
        registerEvent(new FlightStatusChangedEvent(
            getId().getValue(), flightNumber, previous, "DEPARTED", null));
    }

    public void markArrived() {
        assertStatus(FlightStatus.DEPARTED, "mark arrived");
        String previous = this.status.name();
        this.status     = FlightStatus.ARRIVED;
        this.updatedAt  = Instant.now();
        registerEvent(new FlightStatusChangedEvent(
            getId().getValue(), flightNumber, previous, "ARRIVED", null));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public long availableSeatCount(SeatClass seatClass) {
        return seats.stream()
            .filter(s -> s.getSeatClass() == seatClass && s.isAvailable())
            .count();
    }

    public Optional<Seat> findSeatById(SeatId id) {
        return seats.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void assertBookable() {
        if (status == FlightStatus.CANCELLED)
            throw new BusinessRuleViolationException(
                "Cannot book seats on a CANCELLED flight", "FLIGHT_CANCELLED");
        if (status == FlightStatus.DEPARTED || status == FlightStatus.ARRIVED)
            throw new BusinessRuleViolationException(
                "Cannot book seats on a flight that has already departed", "FLIGHT_DEPARTED");
        if (status == FlightStatus.BOARDING)
            throw new BusinessRuleViolationException(
                "Seat reservations are closed — flight is boarding", "FLIGHT_BOARDING");
    }

    private void assertStatus(FlightStatus expected, String operation) {
        if (status != expected)
            throw new BusinessRuleViolationException(
                "Cannot [" + operation + "] when flight is in [" + status + "] state",
                "INVALID_STATUS_TRANSITION");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String       getAirlineCode()  { return airlineCode; }
    public String       getFlightNumber() { return flightNumber; }
    public Route        getRoute()        { return route; }
    public FlightStatus getStatus()       { return status; }
    public List<Seat>   getSeats()        { return Collections.unmodifiableList(seats); }
    public String       getDelayReason()  { return delayReason; }
    public Instant      getCreatedAt()    { return createdAt; }
    public Instant      getUpdatedAt()    { return updatedAt; }

    /**
     * Configuration record for bulk seat loading.
     */
    public record SeatConfiguration(
        String seatNumber, SeatClass seatClass, Money price) {}
}
