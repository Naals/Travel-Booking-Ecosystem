package com.travel.messaging.infrastructure.persistence.mapper;

import com.travel.messaging.domain.aggregate.Message;
import com.travel.messaging.domain.model.MessageStatus;
import com.travel.messaging.domain.valueobject.MessageContent;
import com.travel.messaging.domain.valueobject.MessageId;
import com.travel.messaging.infrastructure.persistence.document.MessageDocument;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageDocument toDocument(Message m) {
        return MessageDocument.builder()
            .id(m.getId().getValue())
            .conversationId(m.getConversationId())
            .senderId(m.getSenderId())
            .recipientId(m.getRecipientId())
            .content(m.getContent().map(MessageContent::getValue).orElse(null))
            .status(m.getStatus().name())
            .sentAt(m.getSentAt())
            .deletedAt(m.getDeletedAt())
            .build();
    }

    public Message toDomain(MessageDocument d) {
        // Bypasses MessageContent.of()'s blank-check deliberately: a
        // deleted message's content is legitimately absent, and
        // reconstitute() restores known-valid (or known-cleared) state
        // rather than re-validating business rules already satisfied
        // at creation time.
        MessageContent content = d.getContent() != null ? MessageContent.of(d.getContent()) : null;

        return Message.reconstitute(
            MessageId.of(d.getId()),
            d.getConversationId(), d.getSenderId(), d.getRecipientId(),
            content, MessageStatus.valueOf(d.getStatus()),
            d.getSentAt(), d.getDeletedAt()
        );
    }
}
