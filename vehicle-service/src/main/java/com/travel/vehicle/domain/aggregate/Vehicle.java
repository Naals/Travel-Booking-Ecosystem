package com.travel.vehicle.domain.aggregate;

import com.travel.vehicle.domain.event.*;
import com.travel.vehicle.domain.model.VehicleRental;
import com.travel.vehicle.domain.valueobject.*;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Vehicle Aggregate Root.
 *
 * Represents a single physical car in the rental fleet.
 * Each Vehicle manages its own rental state — at most one
 * active rental at a time.
 *
 * Key distinction from hotel/flight:
 * - Each vehicle is an independent aggregate (not grouped under a "Fleet" root)
 * - Fleet queries (find available ECONOMY in Istanbul) are handled by
 *   the FleetQueryService, which searches across Vehicle aggregates
 * - Supports one-way rentals via separate pickup and return locations
 *
 * Saga participation (VEHICLE booking type):
 *   BookingCreated → vehicle selected by FleetQueryService →
 *     reserve() → VehicleReservedEvent
 *   PaymentFailed →
 *     releaseReservation() → VehicleReservationReleasedEvent
 *   BookingConfirmed →
 *     confirmRental() → RENTED status
 */
public class Vehicle extends AggregateRoot<VehicleId> {

    private final VehicleSpec    spec;
    private final VehicleCategory category;
    private       VehicleStatus  status;
    private final PickupLocation homeLocation;
    private       PickupLocation currentLocation;
    private       Money          dailyRate;
    private       VehicleRental  activeRental;
    private final Instant        createdAt;
    private       Instant        updatedAt;

    // ── Private constructor ───────────────────────────────────────────────────

    private Vehicle(VehicleId id, VehicleSpec spec, VehicleCategory category,
                    PickupLocation homeLocation, Money dailyRate) {
        super(id);
        this.spec            = spec;
        this.category        = category;
        this.homeLocation    = homeLocation;
        this.currentLocation = homeLocation;
        this.dailyRate       = dailyRate;
        this.status          = VehicleStatus.AVAILABLE;
        this.createdAt       = Instant.now();
        this.updatedAt       = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Adds a new vehicle to the active fleet.
     * Raises VehicleAddedToFleetEvent for search-service indexing.
     */
    public static Vehicle addToFleet(VehicleSpec spec, VehicleCategory category,
                                     PickupLocation homeLocation, Money dailyRate) {
        VehicleId id      = VehicleId.generate();
        Vehicle   vehicle = new Vehicle(id, spec, category, homeLocation, dailyRate);
        vehicle.registerEvent(new VehicleAddedToFleetEvent(
            id.getValue(), category.name(),
            homeLocation.getLocationCode(),
            homeLocation.getCity(),
            homeLocation.getCountry()));
        return vehicle;
    }

    public static Vehicle reconstitute(VehicleId id, VehicleSpec spec,
                                       VehicleCategory category, VehicleStatus status,
                                       PickupLocation homeLocation,
                                       PickupLocation currentLocation,
                                       Money dailyRate, VehicleRental activeRental,
                                       Instant createdAt, Instant updatedAt) {
        Vehicle v = new Vehicle(id, spec, category, homeLocation, dailyRate);
        v.status          = status;
        v.currentLocation = currentLocation;
        v.activeRental    = activeRental;
        return v;
    }

    // ── Availability ──────────────────────────────────────────────────────────

    /**
     * Returns true if this vehicle is available for the requested rental period.
     * A vehicle is available if it has no active rental overlapping the period.
     */
    public boolean isAvailableFor(DateRange rentalPeriod) {
        if (status != VehicleStatus.AVAILABLE) return false;
        if (activeRental == null) return true;
        return !activeRental.getRentalPeriod().overlaps(rentalPeriod);
    }

    // ── Rental lifecycle ──────────────────────────────────────────────────────

    /**
     * Places a rental hold on this vehicle.
     * Raises VehicleReservedEvent → booking-service saga advances.
     *
     * @param oneWayReturn null means same-location return
     */
    public void reserve(String bookingId, String userId,
                        LocalDate pickupDate, LocalDate returnDate,
                        PickupLocation pickupLoc,
                        PickupLocation returnLoc) {
        DateRange period = DateRange.of(pickupDate, returnDate);

        if (!isAvailableFor(period))
            throw new BusinessRuleViolationException(
                "Vehicle " + spec.getLicensePlate() + " is not available for requested dates",
                "VEHICLE_NOT_AVAILABLE");

        PickupLocation effectiveReturn = returnLoc != null ? returnLoc : pickupLoc;
        Money totalPrice = calculatePrice(period);

        this.activeRental = new VehicleRental(
            bookingId, userId, period, pickupLoc, effectiveReturn);
        this.status    = VehicleStatus.RESERVED;
        this.updatedAt = Instant.now();

        registerEvent(new VehicleReservedEvent(
            getId().getValue(), bookingId, userId, category.name(),
            pickupDate, returnDate,
            pickupLoc.getLocationCode(),
            effectiveReturn.getLocationCode(),
            totalPrice));
        registerEvent(new VehicleAvailabilityUpdatedEvent(
            getId().getValue(), currentLocation.getLocationCode()));
    }

    /**
     * Releases the rental hold (compensation transaction).
     * Raises VehicleReservationReleasedEvent → booking-service cancels.
     */
    public void releaseReservation(String bookingId, String reason) {
        if (activeRental == null || !activeRental.getBookingId().equals(bookingId))
            throw new BusinessRuleViolationException(
                "No rental reservation found for bookingId: " + bookingId,
                "RESERVATION_NOT_FOUND");
        this.activeRental = null;
        this.status       = VehicleStatus.AVAILABLE;
        this.updatedAt    = Instant.now();
        registerEvent(new VehicleReservationReleasedEvent(
            getId().getValue(), bookingId, reason));
        registerEvent(new VehicleAvailabilityUpdatedEvent(
            getId().getValue(), currentLocation.getLocationCode()));
    }

    /**
     * Confirms the rental — vehicle transitions to RENTED (permanently until return).
     */
    public void confirmRental(String bookingId) {
        if (activeRental == null || !activeRental.getBookingId().equals(bookingId))
            throw new BusinessRuleViolationException(
                "No rental reservation found for bookingId: " + bookingId,
                "RESERVATION_NOT_FOUND");
        activeRental.confirm();
        this.status    = VehicleStatus.RENTED;
        this.updatedAt = Instant.now();
    }

    /**
     * Customer returns the vehicle. Optionally updates current location
     * for one-way rentals.
     */
    public void processReturn(String bookingId) {
        if (activeRental == null || !activeRental.getBookingId().equals(bookingId))
            throw new BusinessRuleViolationException(
                "No active rental found for bookingId: " + bookingId,
                "RENTAL_NOT_FOUND");

        // Update current location for one-way rentals
        this.currentLocation = activeRental.getReturnLocation();
        this.activeRental    = null;
        this.status          = VehicleStatus.AVAILABLE;
        this.updatedAt       = Instant.now();

        registerEvent(new VehicleAvailabilityUpdatedEvent(
            getId().getValue(), currentLocation.getLocationCode()));
    }

    // ── Fleet management ──────────────────────────────────────────────────────

    public void sendToMaintenance() {
        if (status == VehicleStatus.RESERVED || status == VehicleStatus.RENTED)
            throw new BusinessRuleViolationException(
                "Cannot send a reserved or rented vehicle to maintenance",
                "INVALID_VEHICLE_OPERATION");
        this.status    = VehicleStatus.MAINTENANCE;
        this.updatedAt = Instant.now();
    }

    public void returnFromMaintenance() {
        if (status != VehicleStatus.MAINTENANCE)
            throw new BusinessRuleViolationException(
                "Vehicle is not in maintenance", "INVALID_VEHICLE_OPERATION");
        this.status    = VehicleStatus.AVAILABLE;
        this.updatedAt = Instant.now();
    }

    public void decommission() {
        if (status == VehicleStatus.RENTED)
            throw new BusinessRuleViolationException(
                "Cannot decommission a rented vehicle", "INVALID_VEHICLE_OPERATION");
        this.status    = VehicleStatus.DECOMMISSIONED;
        this.updatedAt = Instant.now();
    }

    public void updateDailyRate(Money newRate) {
        this.dailyRate = newRate;
        this.updatedAt = Instant.now();
    }

    // ── Pricing ───────────────────────────────────────────────────────────────

    /**
     * Calculates total rental price for a given period.
     * Base: dailyRate × days.
     * Future: one-way surcharge, insurance add-ons, seasonal pricing.
     */
    public Money calculatePrice(DateRange rentalPeriod) {
        return dailyRate.multiply(rentalPeriod.days());
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public VehicleSpec      getSpec()            { return spec; }
    public VehicleCategory  getCategory()        { return category; }
    public VehicleStatus    getStatus()          { return status; }
    public PickupLocation   getHomeLocation()    { return homeLocation; }
    public PickupLocation   getCurrentLocation() { return currentLocation; }
    public Money            getDailyRate()       { return dailyRate; }
    public Optional<VehicleRental> getActiveRental() { return Optional.ofNullable(activeRental); }
    public Instant          getCreatedAt()       { return createdAt; }
    public Instant          getUpdatedAt()       { return updatedAt; }

    public boolean isAvailable()     { return status == VehicleStatus.AVAILABLE; }
    public boolean isOneWayRental()  { return activeRental != null && activeRental.isOneWay(); }
}
