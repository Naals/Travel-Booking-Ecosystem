package com.travel.messaging.domain.event;

import com.travel.shared.event.DomainEvent;

public class ConversationBlockedEvent extends DomainEvent {

    private final String conversationId;
    private final String blockedByUserId;

    public ConversationBlockedEvent(String conversationId, String blockedByUserId) {
        super("ConversationBlocked");
        this.conversationId  = conversationId;
        this.blockedByUserId = blockedByUserId;
    }

    @Override public String getAggregateId() { return conversationId; }
    public String getConversationId()  { return conversationId; }
    public String getBlockedByUserId() { return blockedByUserId; }
}
