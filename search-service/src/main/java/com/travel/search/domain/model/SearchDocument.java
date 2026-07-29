package com.travel.search.domain.model;

import com.travel.search.domain.valueobject.GeoCoordinates;
import com.travel.common.exception.DomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Unified read model for the "listings" index.
 *
 * Not an aggregate root — this is a denormalized CQRS projection with
 * no invariants to enforce beyond basic construction validity. The
 * source of truth for each listing lives in its owning service
 * (property-service, hotel-service, flight-service, vehicle-service);
 * this document exists purely to make cross-type search fast.
 *
 * Fields intentionally left nullable (price, coordinates) reflect what
 * the current upstream "listing created" events actually carry — see
 * ADR-007. A production iteration would either enrich those event
 * payloads or have this service call back to the owning service's
 * REST API to backfill missing fields before indexing.
 */
public final class SearchDocument {

    private final String              id;              // matches the resourceId in the owning service
    private final ListingType         listingType;
    private final String              title;
    private final String              description;
    private final String              city;
    private final String              country;
    private final GeoCoordinates      location;         // nullable — not populated by current events
    private final BigDecimal          priceAmount;      // nullable — see class comment
    private final String              currency;
    private final Double              rating;           // nullable until reviews land (Tier 3)
    private final boolean             available;
    private final String              imageUrl;
    private final Map<String, String> attributes;       // type-specific fields
    private final Instant             createdAt;
    private final Instant             updatedAt;
    private final Instant             lastAvailabilityEventAt; // staleness marker, see ADR-007

    private SearchDocument(Builder b) {
        this.id                      = Objects.requireNonNull(b.id, "id is required");
        this.listingType             = Objects.requireNonNull(b.listingType, "listingType is required");
        this.title                   = Objects.requireNonNull(b.title, "title is required");
        this.description             = b.description;
        this.city                    = b.city;
        this.country                 = b.country;
        this.location                = b.location;
        this.priceAmount             = b.priceAmount;
        this.currency                = b.currency;
        this.rating                  = b.rating;
        this.available               = b.available;
        this.imageUrl                = b.imageUrl;
        this.attributes              = b.attributes != null
            ? Collections.unmodifiableMap(b.attributes) : Collections.emptyMap();
        this.createdAt               = b.createdAt != null ? b.createdAt : Instant.now();
        this.updatedAt               = b.updatedAt != null ? b.updatedAt : Instant.now();
        this.lastAvailabilityEventAt = b.lastAvailabilityEventAt;

        if (priceAmount != null && priceAmount.signum() < 0)
            throw new DomainException("priceAmount must not be negative", "INVALID_LISTING");
    }

    public static Builder builder() { return new Builder(); }

    public String              getId()                      { return id; }
    public ListingType         getListingType()              { return listingType; }
    public String              getTitle()                    { return title; }
    public String              getDescription()              { return description; }
    public String              getCity()                     { return city; }
    public String              getCountry()                  { return country; }
    public GeoCoordinates      getLocation()                 { return location; }
    public BigDecimal          getPriceAmount()              { return priceAmount; }
    public String              getCurrency()                 { return currency; }
    public Double              getRating()                   { return rating; }
    public boolean             isAvailable()                 { return available; }
    public String              getImageUrl()                 { return imageUrl; }
    public Map<String, String> getAttributes()               { return attributes; }
    public Instant             getCreatedAt()                { return createdAt; }
    public Instant             getUpdatedAt()                { return updatedAt; }
    public Instant             getLastAvailabilityEventAt()  { return lastAvailabilityEventAt; }

    public static final class Builder {
        private String              id;
        private ListingType         listingType;
        private String              title;
        private String              description;
        private String              city;
        private String              country;
        private GeoCoordinates      location;
        private BigDecimal          priceAmount;
        private String              currency;
        private Double              rating;
        private boolean             available = true;
        private String              imageUrl;
        private Map<String, String> attributes;
        private Instant             createdAt;
        private Instant             updatedAt;
        private Instant             lastAvailabilityEventAt;

        public Builder id(String v)                            { this.id = v; return this; }
        public Builder listingType(ListingType v)               { this.listingType = v; return this; }
        public Builder title(String v)                          { this.title = v; return this; }
        public Builder description(String v)                    { this.description = v; return this; }
        public Builder city(String v)                           { this.city = v; return this; }
        public Builder country(String v)                        { this.country = v; return this; }
        public Builder location(GeoCoordinates v)                { this.location = v; return this; }
        public Builder priceAmount(BigDecimal v)                 { this.priceAmount = v; return this; }
        public Builder currency(String v)                        { this.currency = v; return this; }
        public Builder rating(Double v)                          { this.rating = v; return this; }
        public Builder available(boolean v)                      { this.available = v; return this; }
        public Builder imageUrl(String v)                        { this.imageUrl = v; return this; }
        public Builder attributes(Map<String, String> v)         { this.attributes = v; return this; }
        public Builder createdAt(Instant v)                      { this.createdAt = v; return this; }
        public Builder updatedAt(Instant v)                      { this.updatedAt = v; return this; }
        public Builder lastAvailabilityEventAt(Instant v)        { this.lastAvailabilityEventAt = v; return this; }

        public SearchDocument build() { return new SearchDocument(this); }
    }
}
