package com.travel.user.domain.event;

import com.travel.shared.event.DomainEvent;

public class UserProfileCreatedEvent extends DomainEvent {

    private final String userId;
    private final String displayName;

    public UserProfileCreatedEvent(String userId, String displayName) {
        super("UserProfileCreated");
        this.userId      = userId;
        this.displayName = displayName;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId()      { return userId; }
    public String getDisplayName() { return displayName; }
}
