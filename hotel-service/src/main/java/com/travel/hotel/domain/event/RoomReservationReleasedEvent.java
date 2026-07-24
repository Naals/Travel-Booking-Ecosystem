package com.travel.hotel.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a room reservation is released (compensation transaction).
 * Consumed by booking-service saga as inventory.reservation-released.
 */
public class RoomReservationReleasedEvent extends DomainEvent {

    private final String hotelId;
    private final String roomId;
    private final String bookingId;
    private final String reason;

    public RoomReservationReleasedEvent(String hotelId, String roomId,
                                        String bookingId, String reason) {
        super("RoomReservationReleased");
        this.hotelId   = hotelId;
        this.roomId    = roomId;
        this.bookingId = bookingId;
        this.reason    = reason;
    }

    @Override public String getAggregateId() { return hotelId; }
    public String getHotelId()  { return hotelId; }
    public String getRoomId()   { return roomId; }
    public String getBookingId(){ return bookingId; }
    public String getReason()   { return reason; }
}
