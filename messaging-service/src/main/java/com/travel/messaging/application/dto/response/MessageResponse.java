package com.travel.messaging.application.dto.response;

import com.travel.messaging.domain.aggregate.Message;
import com.travel.messaging.domain.valueobject.MessageContent;

import java.time.Instant;

public record MessageResponse(
    String  messageId,
    String  conversationId,
    String  senderId,
    String  recipientId,
    String  content, // null when the message has been deleted
    String  status,
    Instant sentAt
) {
    public static MessageResponse from(Message m) {
        return new MessageResponse(
            m.getId().getValue(), m.getConversationId(), m.getSenderId(), m.getRecipientId(),
            m.getContent().map(MessageContent::getValue).orElse(null),
            m.getStatus().name(), m.getSentAt());
    }
}
