package com.travel.hotel.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a hotel is activated.
 * Consumed by search-service to index the hotel in Elasticsearch.
 */
public class HotelCreatedEvent extends DomainEvent {

    private final String hotelId;
    private final String name;
    private final String city;
    private final String country;
    private final int    starRating;

    public HotelCreatedEvent(String hotelId, String name,
                             String city, String country, int starRating) {
        super("HotelCreated");
        this.hotelId    = hotelId;
        this.name       = name;
        this.city       = city;
        this.country    = country;
        this.starRating = starRating;
    }

    @Override public String getAggregateId() { return hotelId; }
    public String getHotelId()   { return hotelId; }
    public String getName()      { return name; }
    public String getCity()      { return city; }
    public String getCountry()   { return country; }
    public int    getStarRating(){ return starRating; }
}
