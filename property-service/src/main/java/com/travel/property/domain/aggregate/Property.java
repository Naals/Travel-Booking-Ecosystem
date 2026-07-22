package com.travel.property.domain.aggregate;

import com.travel.property.domain.event.*;
import com.travel.property.domain.valueobject.*;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.DomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Property Aggregate Root.
 *
 * Owns the property listing and its availability calendar.
 * Availability is enforced as a domain invariant — the aggregate
 * refuses to place a reservation if dates overlap with existing holds.
 *
 * Saga participation:
 *   Receives BookingCreated (via domain service) →
 *     if available: placeReservation() → raises ReservationPlacedEvent
 *     if not:       raises ReservationUnavailableEvent
 *   Receives PaymentFailed →
 *     releaseReservation() → raises ReservationReleasedEvent
 *   Receives BookingConfirmed →
 *     confirmReservation() (hold becomes permanent)
 */
public class Property extends AggregateRoot<PropertyId> {

    private final String       hostId;
    private String             title;
    private String             description;
    private PropertyType       type;
    private PropertyStatus     status;
    private Address            address;
    private Money              nightlyRate;
    private int                maxGuests;
    private int                bedrooms;
    private int                bathrooms;
    private Set<Amenity>       amenities;
    private List<Reservation>  reservations;
    private final Instant      createdAt;
    private Instant            updatedAt;

    // ── Private constructor ───────────────────────────────────────────────────

    private Property(PropertyId id, String hostId, String title,
                     String description, PropertyType type,
                     Address address, Money nightlyRate,
                     int maxGuests, int bedrooms, int bathrooms) {
        super(id);
        this.hostId       = hostId;
        this.title        = title;
        this.description  = description;
        this.type         = type;
        this.status       = PropertyStatus.DRAFT;
        this.address      = address;
        this.nightlyRate  = nightlyRate;
        this.maxGuests    = maxGuests;
        this.bedrooms     = bedrooms;
        this.bathrooms    = bathrooms;
        this.amenities    = new HashSet<>();
        this.reservations = new ArrayList<>();
        this.createdAt    = Instant.now();
        this.updatedAt    = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static Property create(String hostId, String title, String description,
                                  PropertyType type, Address address,
                                  Money nightlyRate, int maxGuests,
                                  int bedrooms, int bathrooms) {
        validateCapacity(maxGuests, bedrooms, bathrooms);
        PropertyId id       = PropertyId.generate();
        Property   property = new Property(id, hostId, title, description, type,
            address, nightlyRate, maxGuests,
            bedrooms, bathrooms);
        // Event raised when published, not at creation (status = DRAFT)
        return property;
    }

    public static Property reconstitute(PropertyId id, String hostId,
                                        String title, String description,
                                        PropertyType type, PropertyStatus status,
                                        Address address, Money nightlyRate,
                                        int maxGuests, int bedrooms, int bathrooms,
                                        Set<Amenity> amenities,
                                        List<Reservation> reservations,
                                        Instant createdAt, Instant updatedAt) {
        Property p = new Property(id, hostId, title, description, type,
            address, nightlyRate, maxGuests, bedrooms, bathrooms);
        p.status       = status;
        p.amenities    = amenities != null ? new HashSet<>(amenities) : new HashSet<>();
        p.reservations = reservations != null ? new ArrayList<>(reservations) : new ArrayList<>();
        return p;
    }

    // ── Publishing lifecycle ──────────────────────────────────────────────────

    /**
     * Publishes a DRAFT property — makes it visible in search results.
     * Raises PropertyCreatedEvent for search-service indexing.
     */
    public void publish() {
        if (status != PropertyStatus.DRAFT)
            throw new BusinessRuleViolationException(
                "Only DRAFT properties can be published", "INVALID_STATUS_TRANSITION");
        this.status    = PropertyStatus.ACTIVE;
        this.updatedAt = Instant.now();
        registerEvent(new PropertyCreatedEvent(
            getId().getValue(), hostId, title,
            type.name(), address.getCity(), address.getCountry()));
    }

    public void pause() {
        if (status != PropertyStatus.ACTIVE)
            throw new BusinessRuleViolationException(
                "Only ACTIVE properties can be paused", "INVALID_STATUS_TRANSITION");
        this.status    = PropertyStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    public void reactivate() {
        if (status != PropertyStatus.PAUSED)
            throw new BusinessRuleViolationException(
                "Only PAUSED properties can be reactivated", "INVALID_STATUS_TRANSITION");
        this.status    = PropertyStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status    = PropertyStatus.DEACTIVATED;
        this.updatedAt = Instant.now();
    }

    // ── Availability management ───────────────────────────────────────────────

    /**
     * Checks if the property is available for a given date range.
     * A date range is unavailable if it overlaps with any existing reservation.
     */
    public boolean isAvailable(DateRange requested) {
        if (status != PropertyStatus.ACTIVE)
            return false;
        return reservations.stream()
            .noneMatch(r -> r.getDateRange().overlaps(requested));
    }

    /**
     * Places a reservation hold for the saga.
     * Raises ReservationPlacedEvent → booking-service saga advances.
     *
     * @throws BusinessRuleViolationException if dates are not available
     */
    public void placeReservation(String bookingId, String userId,
                                 LocalDate checkIn, LocalDate checkOut) {
        if (status != PropertyStatus.ACTIVE)
            throw new BusinessRuleViolationException(
                "Property is not available for booking", "PROPERTY_NOT_ACTIVE");

        DateRange requested = DateRange.of(checkIn, checkOut);

        if (!isAvailable(requested))
            throw new BusinessRuleViolationException(
                "Property is not available for the requested dates",
                "DATES_NOT_AVAILABLE");

        reservations.add(new Reservation(bookingId, userId, requested));
        this.updatedAt = Instant.now();

        registerEvent(new ReservationPlacedEvent(
            getId().getValue(), bookingId, userId, checkIn, checkOut));
        registerEvent(new AvailabilityUpdatedEvent(getId().getValue()));
    }

    /**
     * Releases a reservation hold (compensation transaction).
     * Raises ReservationReleasedEvent → booking-service saga completes cancellation.
     */
    public void releaseReservation(String bookingId, String reason) {
        boolean removed = reservations.removeIf(
            r -> r.getBookingId().equals(bookingId));

        if (!removed)
            throw new BusinessRuleViolationException(
                "No reservation found for bookingId: " + bookingId,
                "RESERVATION_NOT_FOUND");

        this.updatedAt = Instant.now();
        registerEvent(new ReservationReleasedEvent(
            getId().getValue(), bookingId, reason));
        registerEvent(new AvailabilityUpdatedEvent(getId().getValue()));
    }

    /**
     * Confirms a reservation hold — booking is now permanently reserved.
     * Called when booking-service publishes BookingConfirmed.
     */
    public void confirmReservation(String bookingId) {
        reservations.stream()
            .filter(r -> r.getBookingId().equals(bookingId))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleViolationException(
                "No reservation found for bookingId: " + bookingId,
                "RESERVATION_NOT_FOUND"))
            .confirm();
        this.updatedAt = Instant.now();
    }

    // ── Property updates ──────────────────────────────────────────────────────

    public void updateDetails(String title, String description,
                              int maxGuests, int bedrooms, int bathrooms) {
        validateCapacity(maxGuests, bedrooms, bathrooms);
        this.title       = title;
        this.description = description;
        this.maxGuests   = maxGuests;
        this.bedrooms    = bedrooms;
        this.bathrooms   = bathrooms;
        this.updatedAt   = Instant.now();
    }

    public void updateNightlyRate(Money newRate) {
        this.nightlyRate = Objects.requireNonNull(newRate, "Nightly rate must not be null");
        this.updatedAt   = Instant.now();
    }

    public void addAmenity(Amenity amenity) {
        this.amenities.add(amenity);
        this.updatedAt = Instant.now();
    }

    public void removeAmenity(Amenity amenity) {
        this.amenities.remove(amenity);
        this.updatedAt = Instant.now();
    }

    /**
     * Calculates total price for a date range.
     * Simple nightly rate × nights calculation.
     * Future: support dynamic pricing, seasonal rates, cleaning fees.
     */
    public Money calculatePrice(DateRange dateRange) {
        long nights = dateRange.nights();
        BigDecimal total = nightlyRate.getAmount()
            .multiply(BigDecimal.valueOf(nights));
        return Money.of(total, nightlyRate.getCurrency());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static void validateCapacity(int maxGuests, int bedrooms, int bathrooms) {
        if (maxGuests < 1)
            throw new DomainException("maxGuests must be at least 1", "INVALID_CAPACITY");
        if (bedrooms < 0)
            throw new DomainException("bedrooms must be non-negative", "INVALID_CAPACITY");
        if (bathrooms < 1)
            throw new DomainException("bathrooms must be at least 1", "INVALID_CAPACITY");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String             getHostId()      { return hostId; }
    public String             getTitle()       { return title; }
    public String             getDescription() { return description; }
    public PropertyType       getType()        { return type; }
    public PropertyStatus     getStatus()      { return status; }
    public Address            getAddress()     { return address; }
    public Money              getNightlyRate() { return nightlyRate; }
    public int                getMaxGuests()   { return maxGuests; }
    public int                getBedrooms()    { return bedrooms; }
    public int                getBathrooms()   { return bathrooms; }
    public Set<Amenity>       getAmenities()   { return Collections.unmodifiableSet(amenities); }
    public List<Reservation>  getReservations(){ return Collections.unmodifiableList(reservations); }
    public Instant            getCreatedAt()   { return createdAt; }
    public Instant            getUpdatedAt()   { return updatedAt; }
}
