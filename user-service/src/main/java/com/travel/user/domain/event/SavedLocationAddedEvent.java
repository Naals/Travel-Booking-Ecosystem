package com.travel.user.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Published when a user bookmarks a location. No consumer yet —
 * intended for a future recommendation-service (Tier 4) to build
 * destination-affinity signals from. Published now rather than
 * withheld until a consumer exists: the cost of publishing is
 * near-zero, and retrofitting event emission into an already-shipped
 * aggregate later is more invasive than emitting it from day one.
 */
public class SavedLocationAddedEvent extends DomainEvent {

    private final String userId;
    private final String savedLocationId;
    private final String label;
    private final String city;
    private final String country;

    public SavedLocationAddedEvent(String userId, String savedLocationId,
                                   String label, String city, String country) {
        super("SavedLocationAdded");
        this.userId          = userId;
        this.savedLocationId = savedLocationId;
        this.label           = label;
        this.city            = city;
        this.country         = country;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId()          { return userId; }
    public String getSavedLocationId() { return savedLocationId; }
    public String getLabel()           { return label; }
    public String getCity()            { return city; }
    public String getCountry()         { return country; }
}
