package com.travel.messaging.domain.event;

import com.travel.shared.event.DomainEvent;

public class MessageDeletedEvent extends DomainEvent {

    private final String messageId;
    private final String conversationId;
    private final String deletedByUserId;

    public MessageDeletedEvent(String messageId, String conversationId, String deletedByUserId) {
        super("MessageDeleted");
        this.messageId       = messageId;
        this.conversationId  = conversationId;
        this.deletedByUserId = deletedByUserId;
    }

    @Override public String getAggregateId() { return messageId; }
    public String getMessageId()       { return messageId; }
    public String getConversationId()  { return conversationId; }
    public String getDeletedByUserId() { return deletedByUserId; }
}
