package com.travel.hotel.domain.event;

import com.travel.shared.event.DomainEvent;
import java.time.LocalDate;

/**
 * Published when a room is reserved for a booking.
 * Consumed by booking-service saga as inventory.reservation-confirmed.
 */
public class RoomReservedEvent extends DomainEvent {

    private final String    hotelId;
    private final String    roomId;
    private final String    bookingId;
    private final String    userId;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;

    public RoomReservedEvent(String hotelId, String roomId, String bookingId,
                             String userId, LocalDate checkInDate,
                             LocalDate checkOutDate) {
        super("RoomReserved");
        this.hotelId      = hotelId;
        this.roomId       = roomId;
        this.bookingId    = bookingId;
        this.userId       = userId;
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    @Override public String getAggregateId() { return hotelId; }
    public String    getHotelId()     { return hotelId; }
    public String    getRoomId()      { return roomId; }
    public String    getBookingId()   { return bookingId; }
    public String    getUserId()      { return userId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate(){ return checkOutDate; }
}
