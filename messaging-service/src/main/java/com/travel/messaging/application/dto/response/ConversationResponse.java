package com.travel.messaging.application.dto.response;

import com.travel.messaging.domain.aggregate.Conversation;

import java.time.Instant;
import java.util.List;

public record ConversationResponse(
    String       conversationId,
    List<String> participantIds,
    String       contextType,
    String       bookingId,
    String       status,
    String       lastMessagePreview,
    String       lastMessageSenderId,
    Instant      lastMessageAt,
    boolean      hasUnread,
    Instant      createdAt
) {
    public static ConversationResponse from(Conversation c, String requestingUserId) {
        return new ConversationResponse(
            c.getId().getValue(),
            List.copyOf(c.getParticipantIds()),
            c.getContext().getType().name(),
            c.getContext().getBookingId(),
            c.getStatus().name(),
            c.getLastMessagePreview(),
            c.getLastMessageSenderId(),
            c.getLastMessageAt(),
            c.hasUnreadMessagesFor(requestingUserId),
            c.getCreatedAt()
        );
    }
}
