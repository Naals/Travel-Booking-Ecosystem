package com.travel.hotel.domain.aggregate;

import com.travel.hotel.domain.event.*;
import com.travel.hotel.domain.model.Room;
import com.travel.hotel.domain.valueobject.*;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Hotel Aggregate Root.
 *
 * Owns a collection of Room entities. Enforces hotel-level invariants:
 * - A hotel must be ACTIVE to accept reservations
 * - Room numbers must be unique within a hotel
 * - Availability checks are delegated to the target Room
 *
 * Saga participation:
 *   BookingCreated (HOTEL type) →
 *     finds first available room of requested type →
 *     room.reserve() → raises RoomReservedEvent
 *     or raises failure event if no room available
 *
 *   PaymentFailed →
 *     room.releaseReservation() → raises RoomReservationReleasedEvent
 *
 *   BookingConfirmed →
 *     room.confirmReservation()
 */
public class Hotel extends AggregateRoot<HotelId> {

    private final String      managerId;
    private       String      name;
    private       String      description;
    private       Address     address;
    private       int         starRating;
    private       HotelStatus status;
    private final List<Room>  rooms;
    private final Instant     createdAt;
    private       Instant     updatedAt;

    // ── Private constructor ───────────────────────────────────────────────────

    private Hotel(HotelId id, String managerId, String name,
                  String description, Address address, int starRating) {
        super(id);
        this.managerId   = managerId;
        this.name        = name;
        this.description = description;
        this.address     = address;
        this.starRating  = validateStarRating(starRating);
        this.status      = HotelStatus.DRAFT;
        this.rooms       = new ArrayList<>();
        this.createdAt   = Instant.now();
        this.updatedAt   = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static Hotel create(String managerId, String name, String description,
                               Address address, int starRating) {
        HotelId id = HotelId.generate();
        return new Hotel(id, managerId, name, description, address, starRating);
    }

    public static Hotel reconstitute(HotelId id, String managerId, String name,
                                     String description, Address address,
                                     int starRating, HotelStatus status,
                                     List<Room> rooms,
                                     Instant createdAt, Instant updatedAt) {
        Hotel hotel = new Hotel(id, managerId, name, description, address, starRating);
        hotel.status    = status;
        hotel.rooms.addAll(rooms != null ? rooms : Collections.emptyList());
        return hotel;
    }

    // ── Hotel lifecycle ───────────────────────────────────────────────────────

    public void activate() {
        if (rooms.isEmpty())
            throw new BusinessRuleViolationException(
                "Cannot activate a hotel with no rooms", "NO_ROOMS");
        if (status != HotelStatus.DRAFT)
            throw new BusinessRuleViolationException(
                "Only DRAFT hotels can be activated", "INVALID_STATUS_TRANSITION");
        this.status    = HotelStatus.ACTIVE;
        this.updatedAt = Instant.now();
        registerEvent(new HotelCreatedEvent(
            getId().getValue(), name,
            address.getCity(), address.getCountry(), starRating));
    }

    public void close() {
        this.status    = HotelStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    // ── Room management ───────────────────────────────────────────────────────

    /**
     * Adds a room to the hotel. Room numbers must be unique within this hotel.
     */
    public Room addRoom(String roomNumber, RoomType roomType,
                        Money ratePerNight, int maxOccupancy) {
        boolean duplicate = rooms.stream()
            .anyMatch(r -> r.getRoomNumber().equals(roomNumber));
        if (duplicate)
            throw new BusinessRuleViolationException(
                "Room number " + roomNumber + " already exists in this hotel",
                "DUPLICATE_ROOM_NUMBER");

        Room room = new Room(RoomId.generate(), getId().getValue(),
            roomNumber, roomType, ratePerNight, maxOccupancy);
        rooms.add(room);
        this.updatedAt = Instant.now();
        return room;
    }

    public void removeRoom(RoomId roomId) {
        boolean removed = rooms.removeIf(r -> r.getId().equals(roomId));
        if (!removed)
            throw new ResourceNotFoundException("Room", roomId.getValue());
        this.updatedAt = Instant.now();
    }

    // ── Saga participation ────────────────────────────────────────────────────

    /**
     * Reserves the first available room of the requested type.
     * Raises RoomReservedEvent → booking-service saga advances.
     *
     * @throws BusinessRuleViolationException if no room available
     */
    public Room reserveRoom(String bookingId, String userId,
                            RoomType roomType, LocalDate checkIn,
                            LocalDate checkOut) {
        if (status != HotelStatus.ACTIVE)
            throw new BusinessRuleViolationException(
                "Hotel is not accepting reservations", "HOTEL_NOT_ACTIVE");

        DateRange requested = DateRange.of(checkIn, checkOut);

        Room room = rooms.stream()
            .filter(r -> r.getRoomType() == roomType)
            .filter(r -> r.isAvailableFor(requested))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleViolationException(
                "No " + roomType.name() + " rooms available for requested dates",
                "NO_ROOMS_AVAILABLE"));

        room.reserve(bookingId, userId, requested);
        this.updatedAt = Instant.now();

        registerEvent(new RoomReservedEvent(
            getId().getValue(), room.getId().getValue(),
            bookingId, userId, checkIn, checkOut));
        registerEvent(new RoomInventoryUpdatedEvent(getId().getValue()));

        return room;
    }

    /**
     * Releases a room reservation (compensation transaction).
     * Raises RoomReservationReleasedEvent → booking-service saga cancels.
     */
    public void releaseReservation(String bookingId, String reason) {
        rooms.stream()
            .filter(r -> r.getReservations().stream()
                .anyMatch(res -> res.getBookingId().equals(bookingId)))
            .findFirst()
            .ifPresentOrElse(
                room -> {
                    room.releaseReservation(bookingId);
                    this.updatedAt = Instant.now();
                    registerEvent(new RoomReservationReleasedEvent(
                        getId().getValue(), room.getId().getValue(),
                        bookingId, reason));
                    registerEvent(new RoomInventoryUpdatedEvent(getId().getValue()));
                },
                () -> { throw new BusinessRuleViolationException(
                    "No reservation found for bookingId: " + bookingId,
                    "RESERVATION_NOT_FOUND"); });
    }

    public void confirmReservation(String bookingId) {
        rooms.stream()
            .filter(r -> r.getReservations().stream()
                .anyMatch(res -> res.getBookingId().equals(bookingId)))
            .findFirst()
            .ifPresent(room -> {
                room.confirmReservation(bookingId);
                this.updatedAt = Instant.now();
            });
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Room> getAvailableRooms(RoomType type, DateRange dateRange) {
        return rooms.stream()
            .filter(r -> r.getRoomType() == type)
            .filter(r -> r.isAvailableFor(dateRange))
            .toList();
    }

    public Optional<Room> findRoomById(RoomId roomId) {
        return rooms.stream().filter(r -> r.getId().equals(roomId)).findFirst();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static int validateStarRating(int stars) {
        if (stars < 1 || stars > 5)
            throw new com.travel.common.exception.DomainException(
                "Star rating must be between 1 and 5", "INVALID_STAR_RATING");
        return stars;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String      getManagerId()  { return managerId; }
    public String      getName()       { return name; }
    public String      getDescription(){ return description; }
    public Address     getAddress()    { return address; }
    public int         getStarRating() { return starRating; }
    public HotelStatus getStatus()     { return status; }
    public List<Room>  getRooms()      { return Collections.unmodifiableList(rooms); }
    public Instant     getCreatedAt()  { return createdAt; }
    public Instant     getUpdatedAt()  { return updatedAt; }
}
