package com.travel.messaging.infrastructure.persistence.mapper;

import com.travel.messaging.domain.aggregate.Conversation;
import com.travel.messaging.domain.model.ConversationStatus;
import com.travel.messaging.domain.valueobject.ConversationContext;
import com.travel.messaging.domain.valueobject.ConversationId;
import com.travel.messaging.infrastructure.persistence.document.ConversationDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Component
public class ConversationMapper {

    public ConversationDocument toDocument(Conversation c) {
        return ConversationDocument.builder()
            .id(c.getId().getValue())
            .participantIds(new ArrayList<>(c.getParticipantIds()))
            .contextType(c.getContext().getType().name())
            .bookingId(c.getContext().getBookingId())
            .status(c.getStatus().name())
            .lastMessagePreview(c.getLastMessagePreview())
            .lastMessageSenderId(c.getLastMessageSenderId())
            .lastMessageAt(c.getLastMessageAt())
            .lastReadAt(new HashMap<>(c.getLastReadAt()))
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }

    public Conversation toDomain(ConversationDocument d) {
        ConversationContext context = "BOOKING".equals(d.getContextType())
            ? ConversationContext.forBooking(d.getBookingId())
            : ConversationContext.direct();

        return Conversation.reconstitute(
            ConversationId.of(d.getId()),
            new HashSet<>(d.getParticipantIds()),
            context,
            ConversationStatus.valueOf(d.getStatus()),
            d.getLastMessagePreview(),
            d.getLastMessageSenderId(),
            d.getLastMessageAt(),
            d.getLastReadAt(),
            d.getCreatedAt(),
            d.getUpdatedAt()
        );
    }
}
