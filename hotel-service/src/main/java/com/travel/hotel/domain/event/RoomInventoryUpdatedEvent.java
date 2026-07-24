package com.travel.hotel.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when room inventory changes (reservation placed or released).
 * Consumed by search-service to update Elasticsearch availability data.
 */
public class RoomInventoryUpdatedEvent extends DomainEvent {

    private final String hotelId;

    public RoomInventoryUpdatedEvent(String hotelId) {
        super("RoomInventoryUpdated");
        this.hotelId = hotelId;
    }

    @Override public String getAggregateId() { return hotelId; }
    public String getHotelId() { return hotelId; }
}
