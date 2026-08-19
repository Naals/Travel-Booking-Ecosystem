package com.travel.messaging.infrastructure.persistence.repository;

import com.travel.messaging.infrastructure.persistence.document.ConversationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMongoRepository extends MongoRepository<ConversationDocument, String> {

    Page<ConversationDocument> findByParticipantIdsContaining(String userId, Pageable pageable);
    long                        countByParticipantIdsContaining(String userId);

    /**
     * $all + $size:2 against the participantIds array ensures an exact
     * match on both IDs — not merely "contains at least one of them".
     */
    @Query("{ 'participantIds': { $all: ?0, $size: 2 }, 'contextType': 'DIRECT' }")
    Optional<ConversationDocument> findDirectConversationBetween(List<String> participantIds);
}
