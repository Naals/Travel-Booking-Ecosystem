package com.travel.messaging.domain.event;

import com.travel.shared.event.DomainEvent;

public class ConversationStartedEvent extends DomainEvent {

    private final String conversationId;
    private final String initiatorId;
    private final String recipientId;
    private final String contextType;
    private final String bookingId; // nullable

    public ConversationStartedEvent(String conversationId, String initiatorId, String recipientId,
                                    String contextType, String bookingId) {
        super("ConversationStarted");
        this.conversationId = conversationId;
        this.initiatorId    = initiatorId;
        this.recipientId    = recipientId;
        this.contextType    = contextType;
        this.bookingId      = bookingId;
    }

    @Override public String getAggregateId() { return conversationId; }
    public String getConversationId() { return conversationId; }
    public String getInitiatorId()    { return initiatorId; }
    public String getRecipientId()    { return recipientId; }
    public String getContextType()    { return contextType; }
    public String getBookingId()      { return bookingId; }
}
