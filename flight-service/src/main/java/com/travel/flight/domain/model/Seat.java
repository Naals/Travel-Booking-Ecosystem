package com.travel.flight.domain.model;

import com.travel.flight.domain.valueobject.*;
import com.travel.shared.domain.Entity;
import com.travel.common.exception.BusinessRuleViolationException;

import java.util.Optional;

/**
 * Seat entity — owned by the Flight aggregate root.
 *
 * Key difference from Hotel Room:
 * - A seat is reserved for a single specific flight (not a date range)
 * - A seat can hold only ONE reservation at a time
 * - Seat number format: row + column, e.g. "12A", "1C", "34F"
 */
public class Seat extends Entity<SeatId> {

    private final String         flightId;
    private final String         seatNumber;
    private final SeatClass      seatClass;
    private       SeatStatus     status;
    private       Money          price;
    private       SeatReservation reservation; // at most one

    public Seat(SeatId id, String flightId, String seatNumber,
                SeatClass seatClass, Money price) {
        super(id);
        this.flightId   = flightId;
        this.seatNumber = seatNumber;
        this.seatClass  = seatClass;
        this.status     = SeatStatus.AVAILABLE;
        this.price      = price;
    }

    // ── Availability ──────────────────────────────────────────────────────────

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    // ── Reservation management ────────────────────────────────────────────────

    /**
     * Places a reservation hold on this seat.
     * A seat can only have one active reservation.
     */
    public void reserve(String bookingId, String userId) {
        if (!isAvailable())
            throw new BusinessRuleViolationException(
                "Seat " + seatNumber + " is not available (status=" + status + ")",
                "SEAT_NOT_AVAILABLE");
        this.reservation = new SeatReservation(bookingId, userId);
        this.status      = SeatStatus.RESERVED;
    }

    /**
     * Releases the reservation hold (compensation transaction).
     * Seat returns to AVAILABLE.
     */
    public void releaseReservation(String bookingId) {
        if (reservation == null || !reservation.getBookingId().equals(bookingId))
            throw new BusinessRuleViolationException(
                "No reservation found for bookingId: " + bookingId,
                "RESERVATION_NOT_FOUND");
        this.reservation = null;
        this.status      = SeatStatus.AVAILABLE;
    }

    /**
     * Confirms the reservation — seat becomes permanently OCCUPIED.
     */
    public void confirmReservation(String bookingId) {
        if (reservation == null || !reservation.getBookingId().equals(bookingId))
            throw new BusinessRuleViolationException(
                "No reservation found for bookingId: " + bookingId,
                "RESERVATION_NOT_FOUND");
        this.reservation.confirm();
        this.status = SeatStatus.OCCUPIED;
    }

    public void block() {
        if (status == SeatStatus.RESERVED || status == SeatStatus.OCCUPIED)
            throw new BusinessRuleViolationException(
                "Cannot block a reserved or occupied seat", "INVALID_SEAT_OPERATION");
        this.status = SeatStatus.BLOCKED;
    }

    public void unblock() {
        if (status != SeatStatus.BLOCKED)
            throw new BusinessRuleViolationException(
                "Can only unblock a BLOCKED seat", "INVALID_SEAT_OPERATION");
        this.status = SeatStatus.AVAILABLE;
    }

    public void updatePrice(Money newPrice) {
        this.price = newPrice;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String             getFlightId()   { return flightId; }
    public String             getSeatNumber() { return seatNumber; }
    public SeatClass          getSeatClass()  { return seatClass; }
    public SeatStatus         getStatus()     { return status; }
    public Money              getPrice()      { return price; }
    public Optional<SeatReservation> getReservation() {
        return Optional.ofNullable(reservation);
    }
}
