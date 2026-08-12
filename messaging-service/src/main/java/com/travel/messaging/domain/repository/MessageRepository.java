package com.travel.messaging.domain.repository;

import com.travel.messaging.domain.aggregate.Message;
import com.travel.messaging.domain.valueobject.MessageId;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message           save(Message message);
    Optional<Message> findById(MessageId id);
    List<Message>     findByConversationId(String conversationId, int page, int size);
    long                countByConversationId(String conversationId);
}
