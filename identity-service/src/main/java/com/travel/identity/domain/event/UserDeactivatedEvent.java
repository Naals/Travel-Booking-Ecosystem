package com.travel.identity.domain.event;

import com.travel.shared.event.DomainEvent;

public class UserDeactivatedEvent extends DomainEvent {

    private final String userId;
    private final String email;
    private final String reason;

    public UserDeactivatedEvent(String userId, String email, String reason) {
        super("UserDeactivated");
        this.userId = userId;
        this.email  = email;
        this.reason = reason;
    }

    @Override public String getAggregateId() { return userId; }
    public String getUserId() { return userId; }
    public String getEmail()  { return email; }
    public String getReason() { return reason; }
}
