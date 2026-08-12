package com.travel.messaging.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * First real producer of KafkaTopics.MESSAGE_SENT — that constant was
 * declared in common-lib back on Day 3, ahead of any service that
 * would actually publish to it (the same "seeded ahead of its
 * producer" pattern as REVIEW_CREATED/REVIEW_MODERATED, used starting
 * Day 16).
 *
 * contentPreview, not the full message body — see MessageContent.preview().
 * Consumers (notification-service, this day) get enough to render "new
 * message from X: '...'" without carrying the full conversation content
 * through Kafka.
 */
public class MessageSentEvent extends DomainEvent {

    private final String messageId;
    private final String conversationId;
    private final String senderId;
    private final String recipientId;
    private final String contentPreview;

    public MessageSentEvent(String messageId, String conversationId, String senderId,
                            String recipientId, String contentPreview) {
        super("MessageSent");
        this.messageId      = messageId;
        this.conversationId = conversationId;
        this.senderId       = senderId;
        this.recipientId    = recipientId;
        this.contentPreview = contentPreview;
    }

    @Override public String getAggregateId() { return messageId; }
    public String getMessageId()      { return messageId; }
    public String getConversationId() { return conversationId; }
    public String getSenderId()       { return senderId; }
    public String getRecipientId()    { return recipientId; }
    public String getContentPreview() { return contentPreview; }
}
