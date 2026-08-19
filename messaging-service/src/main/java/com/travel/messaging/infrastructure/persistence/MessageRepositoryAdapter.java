package com.travel.messaging.infrastructure.persistence;

import com.travel.messaging.domain.aggregate.Message;
import com.travel.messaging.domain.repository.MessageRepository;
import com.travel.messaging.domain.valueobject.MessageId;
import com.travel.messaging.infrastructure.persistence.mapper.MessageMapper;
import com.travel.messaging.infrastructure.persistence.repository.MessageMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageMongoRepository mongo;
    private final MessageMapper          mapper;

    @Override public Message save(Message m) { return mapper.toDomain(mongo.save(mapper.toDocument(m))); }
    @Override public Optional<Message> findById(MessageId id) { return mongo.findById(id.getValue()).map(mapper::toDomain); }

    @Override
    public List<Message> findByConversationId(String conversationId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        return mongo.findByConversationId(conversationId, pageable).stream().map(mapper::toDomain).toList();
    }

    @Override public long countByConversationId(String conversationId) { return mongo.countByConversationId(conversationId); }
}
