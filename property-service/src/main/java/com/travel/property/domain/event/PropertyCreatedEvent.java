package com.travel.property.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a new property listing goes live.
 * Consumed by search-service to index the property in Elasticsearch.
 */
public class PropertyCreatedEvent extends DomainEvent {

    private final String propertyId;
    private final String hostId;
    private final String title;
    private final String propertyType;
    private final String city;
    private final String country;

    public PropertyCreatedEvent(String propertyId, String hostId, String title,
                                String propertyType, String city, String country) {
        super("PropertyCreated");
        this.propertyId   = propertyId;
        this.hostId       = hostId;
        this.title        = title;
        this.propertyType = propertyType;
        this.city         = city;
        this.country      = country;
    }

    @Override public String getAggregateId() { return propertyId; }
    public String getPropertyId()   { return propertyId; }
    public String getHostId()       { return hostId; }
    public String getTitle()        { return title; }
    public String getPropertyType() { return propertyType; }
    public String getCity()         { return city; }
    public String getCountry()      { return country; }
}
