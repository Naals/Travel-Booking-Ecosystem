package com.travel.messaging.domain.repository;

import com.travel.messaging.domain.aggregate.Conversation;
import com.travel.messaging.domain.valueobject.ConversationId;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {
    Conversation           save(Conversation conversation);
    Optional<Conversation> findById(ConversationId id);
    List<Conversation>     findByParticipantId(String userId, int page, int size);
    long                    countByParticipantId(String userId);

    /** Used to reuse an existing DIRECT thread rather than creating duplicates. */
    Optional<Conversation> findDirectConversationBetween(String userId1, String userId2);
}
