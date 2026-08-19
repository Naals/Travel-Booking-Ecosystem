package com.travel.messaging.infrastructure.persistence;

import com.travel.messaging.domain.aggregate.Conversation;
import com.travel.messaging.domain.repository.ConversationRepository;
import com.travel.messaging.domain.valueobject.ConversationId;
import com.travel.messaging.infrastructure.persistence.mapper.ConversationMapper;
import com.travel.messaging.infrastructure.persistence.repository.ConversationMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationMongoRepository mongo;
    private final ConversationMapper          mapper;

    @Override public Conversation save(Conversation c) { return mapper.toDomain(mongo.save(mapper.toDocument(c))); }
    @Override public Optional<Conversation> findById(ConversationId id) { return mongo.findById(id.getValue()).map(mapper::toDomain); }

    @Override
    public List<Conversation> findByParticipantId(String userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        return mongo.findByParticipantIdsContaining(userId, pageable).stream().map(mapper::toDomain).toList();
    }

    @Override public long countByParticipantId(String userId) { return mongo.countByParticipantIdsContaining(userId); }

    @Override
    public Optional<Conversation> findDirectConversationBetween(String userId1, String userId2) {
        return mongo.findDirectConversationBetween(List.of(userId1, userId2)).map(mapper::toDomain);
    }
}
