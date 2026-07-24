package com.travel.hotel.domain.model;

import com.travel.hotel.domain.valueobject.*;
import com.travel.shared.domain.Entity;
import com.travel.common.exception.BusinessRuleViolationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Room entity — owned by the Hotel aggregate root.
 * Tracks its own reservations and enforces date overlap invariants.
 *
 * Room is NOT a separate aggregate — its lifecycle is bound to the hotel.
 * All mutations go through Hotel, which delegates to Room methods.
 */
public class Room extends Entity<RoomId> {

    private final String        hotelId;
    private final String        roomNumber;
    private final RoomType      roomType;
    private       RoomStatus    status;
    private       Money         ratePerNight;
    private final int           maxOccupancy;
    private final List<RoomReservation> reservations;

    public Room(RoomId id, String hotelId, String roomNumber,
                RoomType roomType, Money ratePerNight, int maxOccupancy) {
        super(id);
        this.hotelId      = hotelId;
        this.roomNumber   = roomNumber;
        this.roomType     = roomType;
        this.status       = RoomStatus.AVAILABLE;
        this.ratePerNight = ratePerNight;
        this.maxOccupancy = maxOccupancy;
        this.reservations = new ArrayList<>();
    }

    // ── Availability ──────────────────────────────────────────────────────────

    public boolean isAvailableFor(DateRange requested) {
        if (status != RoomStatus.AVAILABLE) return false;
        return reservations.stream()
            .noneMatch(r -> r.getDateRange().overlaps(requested));
    }

    // ── Reservation management ────────────────────────────────────────────────

    public void reserve(String bookingId, String userId, DateRange dateRange) {
        if (status != RoomStatus.AVAILABLE)
            throw new BusinessRuleViolationException(
                "Room " + roomNumber + " is not available", "ROOM_NOT_AVAILABLE");

        if (!isAvailableFor(dateRange))
            throw new BusinessRuleViolationException(
                "Room " + roomNumber + " is not available for the requested dates",
                "DATES_NOT_AVAILABLE");

        reservations.add(new RoomReservation(bookingId, userId, dateRange));
    }

    public void releaseReservation(String bookingId) {
        boolean removed = reservations.removeIf(
            r -> r.getBookingId().equals(bookingId));
        if (!removed)
            throw new BusinessRuleViolationException(
                "No reservation found for bookingId: " + bookingId,
                "RESERVATION_NOT_FOUND");
    }

    public void confirmReservation(String bookingId) {
        reservations.stream()
            .filter(r -> r.getBookingId().equals(bookingId))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleViolationException(
                "No reservation found for bookingId: " + bookingId,
                "RESERVATION_NOT_FOUND"))
            .confirm();
    }

    // ── Pricing ───────────────────────────────────────────────────────────────

    /**
     * Calculates total price for a date range.
     * Future: seasonal pricing, weekday/weekend rates, demand surges.
     */
    public Money calculatePrice(DateRange dateRange) {
        return ratePerNight.multiply(dateRange.nights());
    }

    // ── Status management ─────────────────────────────────────────────────────

    public void putInMaintenance() {
        this.status = RoomStatus.MAINTENANCE;
    }

    public void makeAvailable() {
        this.status = RoomStatus.AVAILABLE;
    }

    public void deactivate() {
        this.status = RoomStatus.DEACTIVATED;
    }

    public void updateRate(Money newRate) {
        this.ratePerNight = newRate;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String       getHotelId()     { return hotelId; }
    public String       getRoomNumber()  { return roomNumber; }
    public RoomType     getRoomType()    { return roomType; }
    public RoomStatus   getStatus()      { return status; }
    public Money        getRatePerNight(){ return ratePerNight; }
    public int          getMaxOccupancy(){ return maxOccupancy; }
    public List<RoomReservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }
}
