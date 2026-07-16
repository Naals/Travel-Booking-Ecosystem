package com.travel.identity.domain.event;

import com.travel.shared.event.DomainEvent;

public class UserRegisteredEvent extends DomainEvent {

    private final String userId;
    private final String email;
    private final String fullName;

    public UserRegisteredEvent(String userId, String email, String fullName) {
        super("UserRegistered");
        this.userId   = userId;
        this.email    = email;
        this.fullName = fullName;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId()   { return userId; }
    public String getEmail()    { return email; }
    public String getFullName() { return fullName; }
}
